package com.datadoghq.assessment

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.datadoghq.assessment.Alert.HttpAlert
import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogEntry.HttpLogEntry.{Get, Http, Request, Resource}
import com.datadoghq.assessment.Monitor.HttpMonitor.{HttpAlerter, Props}
import com.datadoghq.assessment.TestUtils.TestEventBus
import org.scalatest.OptionValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Outcome, Retries}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class HttpAlerterSpec extends AnyWordSpec with Matchers with Retries {

  val numberOfRuns = 100

  override def withFixture(test: NoArgTest): Outcome = {
    withFixture(test, numberOfRuns)
  }

  def withFixture(test: NoArgTest, count: Int): Outcome = {
    val outcome = super.withFixture(test)
    outcome match {
      case _ => if (count == 1) super.withFixture(test) else withFixture(test, count - 1)
    }
  }

  lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit lazy val contextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit lazy val timer: Timer[IO] = IO.timer(ec)

  "Alerter" when {
    "alert is off and number of requests exceeds the threshold" should {
      "set status to On and trigger an alert" in {
        val props = Props(
          metricsIntervalMillis = 1000,
          thresholdPerSec = 10,
          alertIntervalMillis = 1000)

        val latencyFactor = 2
        val timeWindow = 3 * props.alertIntervalMillis

        val delayMillis = props.alertIntervalMillis / props.thresholdPerSec / latencyFactor
        val traffic = timeWindow / delayMillis
        val eventBus = new TestEventBus()

        val entries = createLogEntries(traffic, delayMillis)
        val alerter = new HttpAlerter(eventBus, props)

        entries.map(e => alerter.record(e)).sequence_.unsafeRunSync()

        eventBus.events.size shouldBe 1

        eventBus.events.headOption.value should matchPattern {
          case HttpAlert("/api", Alert.On, _, hits, _) if hits >= props.thresholdPerSec =>
        }
      }
    }
  }

  "Alerter" when {
    "alert is on and number of requests exceeds the threshold" should {
      "not trigger an alert" in {
        val props = Props(
          metricsIntervalMillis = 1000,
          thresholdPerSec = 10,
          alertIntervalMillis = 1000)

        val delayFactor = 2
        val timeWindow = 3 * props.alertIntervalMillis

        val delayMillis = props.alertIntervalMillis / props.thresholdPerSec / delayFactor
        val traffic = timeWindow / delayMillis
        val eventBus = new TestEventBus()

        val entries = createLogEntries(traffic, delayMillis)
        val alerter = new HttpAlerter(eventBus, props)

        entries.map(e => alerter.record(e)).sequence_.unsafeRunSync()

        eventBus.events.size shouldBe 1

        eventBus.events.headOption.value should matchPattern {
          case HttpAlert("/api", Alert.On, _, hits, _) if hits >= props.thresholdPerSec =>
        }
      }
    }
  }


  "Alerter" when {
    "alert is on and number of requests drops below the threshold" should {
      "set status to Off and trigger an alert" in {
        val props = Props(
          metricsIntervalMillis = 1000,
          thresholdPerSec = 10,
          alertIntervalMillis = 1000)


        val eventBus = new TestEventBus()
        val alerter = new HttpAlerter(eventBus, props)
        val latencyFactor = 2
        val timeWindow = 3 * props.alertIntervalMillis

        // equivalent to ((props.alertIntervalInMillis / 1000.0) / props.thresholdPerSec) * 1000 / latencyFactor
        val shortDelayMillis = props.alertIntervalMillis / props.thresholdPerSec / latencyFactor
        val heavyTraffic = timeWindow / shortDelayMillis

        val normalDelayMillis = props.alertIntervalMillis / props.thresholdPerSec * latencyFactor
        val normalTraffic = timeWindow / normalDelayMillis

        val heavyTrafficEntries = createLogEntries(heavyTraffic, shortDelayMillis)

        val entries = heavyTrafficEntries ++
          createLogEntries(normalTraffic, normalDelayMillis, heavyTrafficEntries.last.timestamp * 1000 + normalDelayMillis)

        entries.map(e => alerter.record(e)).sequence_.unsafeRunSync()

        eventBus.events.size shouldBe 2

        eventBus.events.headOption.value should matchPattern {
          case HttpAlert("/api", Alert.On, _, hits, _) if hits >= props.thresholdPerSec =>
        }

        eventBus.events(1) should matchPattern {
          case HttpAlert("/api", Alert.Off, _, hits, _) if hits < props.thresholdPerSec =>
        }
      }
    }
  }

  def createLogEntries(n: Int, delayInMills: Long, startTimeMillis: Long = System.currentTimeMillis()): List[HttpLogEntry] = {
    @scala.annotation.tailrec
    def create(left: Int, res: ListBuffer[HttpLogEntry], nextTime: Long): List[HttpLogEntry] = {
      if (left == 0) res.toList
      else create(left - 1, res += HttpLogEntry(
        ip = "10.0.0.2",
        connOwner = "-",
        userId = "apache",
        timestamp = nextTime / 1000L, // convert to unix time
        request = Request(Get, Resource("/api/user", Vector("/api", "/user")), Http("1.0")),
        status = 200,
        bytes = 1234
      ), nextTime + delayInMills)
    }

    create(n, ListBuffer.empty, startTimeMillis)
  }


}
