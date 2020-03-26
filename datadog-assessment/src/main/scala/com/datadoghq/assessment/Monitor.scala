package com.datadoghq.assessment

import java.time.Instant

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.datadoghq.assessment.Alert._
import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.Metrics._
import com.datadoghq.assessment.Monitor.HttpMonitor.{HttpAlerter, HttpMetricsCollector, Props}

import scala.collection.mutable

trait Monitor[T <: LogEntry] {
  def record(logEntry: T): IO[Unit]
}

object Monitor {

  def httpMonitor(props: Props, eventBus: EventBus)(implicit ctxShift: ContextShift[IO]): IO[Monitor[HttpLogEntry]] = {
    IO(new HttpMonitor(
      metricsCollector = new HttpMetricsCollector(eventBus, props),
      alerter = new HttpAlerter(eventBus, props)))
  }

  class HttpMonitor(metricsCollector: MetricsCollector[HttpLogEntry, HttpMetrics],
                    alerter: Alerter[HttpLogEntry])(implicit ctxShift: ContextShift[IO]) extends Monitor[HttpLogEntry] {

    def record(logEntry: HttpLogEntry): IO[Unit] = {
      alerter.record(logEntry) >> metricsCollector.record(logEntry)
    }

  }

  object HttpMonitor {

    /**
     * Properties for HttpMonitor.
     *
     * @param metricsIntervalMillis interval in millis to collect stats
     * @param thresholdPerSec       max number of hits per {{{thresholdIntervalMills}}}
     * @param alertIntervalMillis   interval in millis to trigger alerts
     */
    case class Props(metricsIntervalMillis: Int,
                     thresholdPerSec: Int,
                     alertIntervalMillis: Int)

    class HttpMetricsCollector(eventBus: EventBus, props: Props)(implicit ctxShift: ContextShift[IO])
      extends MetricsCollector[HttpLogEntry, HttpMetrics] {
      private var deadline: Instant = _
      private val metricsMap = mutable.Map[String, HttpMetrics]()

      override def record(logEntry: HttpLogEntry): IO[Unit] = {
        IO.suspend {
          val cur = Instant.ofEpochSecond(logEntry.timestamp)
          if (deadline == null || cur.equals(deadline) || cur.isAfter(deadline)) {
            IO {
              deadline = cur.plusMillis(props.metricsIntervalMillis)
            } >> submitMetrics >> IO(metricsMap.clear())
          } else IO.unit
        } >> update(logEntry)
      }

      private def update(logEntry: HttpLogEntry): IO[Unit] = IO {
        val key = logEntry.request.resource.firstSection
        if (!metricsMap.contains(key)) {
          metricsMap += key -> new HttpMetrics(logEntry.request.resource)
        }
        metricsMap(key).record(logEntry)
      }

      private def submitMetrics: IO[Unit] = {
        metricsMap.values.map(m => eventBus.publish(m)).toList.parSequence_
      }

    }

    class HttpAlerter(eventBus: EventBus, props: Props) extends Alerter[HttpLogEntry] {

      private val statsMap = mutable.Map[String, Stats]()
      private var alertDeadLine: Instant = _
      private val alertIntervalInSec = props.alertIntervalMillis / 1000

      override def record(logEntry: HttpLogEntry): IO[Unit] = {
        IO.suspend {
          val cur = Instant.ofEpochSecond(logEntry.timestamp)
          val report =
            if (alertDeadLine == null || cur.equals(alertDeadLine) || cur.isAfter(alertDeadLine)) {
              IO {
                alertDeadLine = cur.plusMillis(props.alertIntervalMillis)
              } >> updateStatus(logEntry.timestamp)
            } else IO.unit
          report >> update(logEntry)
        }
      }

      private[assessment] def update(logEntry: HttpLogEntry): IO[Unit] = {
        val key = logEntry.request.resource.firstSection
        IO {
          if (!statsMap.contains(key)) statsMap += key -> new Stats(key)
        } >> IO {
          val stats = statsMap(key)
          stats.incHits()
          stats.addBytes(logEntry.bytes)
        }
      }

      private[assessment] def updateStatus(stats: Stats, newStatus: Alert.Status, timestamp: Long): IO[Unit] = {
        for {
          updated <- IO(stats.alertStatus(newStatus))
          _ <- if (updated) eventBus.publish(HttpAlert(stats.resource, newStatus, timestamp, stats.hits, stats.totalSizeInBytes))
               else IO.unit
          _ <- IO(stats.reset())
        } yield ()
      }

      private[assessment] def updateStatus(timestamp: Long): IO[Unit] = {
        statsMap.values.map { stats =>
          val hitsPerSec = stats.hits / alertIntervalInSec
          if (hitsPerSec >= props.thresholdPerSec) {
            updateStatus(stats, On, timestamp)
          } else {
            updateStatus(stats, Off, timestamp)
          }
        }.toList.sequence_
      }


      class Stats(val resource: String) {

        private var _alertStatus: Alert.Status = Off
        private var _hits = 0
        private var _totalSizeInBytes = 0

        def addBytes(value: Int): Unit = {
          _totalSizeInBytes = _totalSizeInBytes + value
        }

        def incHits(): Unit = {
          _hits = _hits + 1
        }

        def alertStatus(newStatus: Alert.Status): Boolean = {
          val updated = _alertStatus != newStatus
          _alertStatus = newStatus
          updated
        }

        def alertStatus: Alert.Status = _alertStatus

        def hits: Int = _hits

        def totalSizeInBytes: Int = _totalSizeInBytes

        def reset(): Unit = {
          _hits = 0
          _totalSizeInBytes = 0
        }
      }

    }

  }

}
