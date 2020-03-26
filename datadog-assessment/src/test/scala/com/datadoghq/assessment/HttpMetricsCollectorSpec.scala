package com.datadoghq.assessment

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogEntry.HttpLogEntry.{Get, Http, Request, Resource}
import com.datadoghq.assessment.Metrics.HttpMetrics
import com.datadoghq.assessment.Monitor.HttpMonitor.{HttpMetricsCollector, Props}
import com.datadoghq.assessment.TestUtils.TestEventBus
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Outcome, Retries}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class HttpMetricsCollectorSpec extends AnyFunSuite with Matchers with Retries {

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

  test("collect") {
    val props = Props(
      metricsIntervalMillis = 1000,
      thresholdPerSec = 10,
      alertIntervalMillis = 1000)


    val eventBus = new TestEventBus()

    val collector = new HttpMetricsCollector(eventBus, props)

    val apiRequest = Request(Get, Resource("/api/user", Vector("/api", "/user")), Http("1.0"))
    val reportRequest = Request(Get, Resource("/report", Vector("/report")), Http("1.0"))
    val totalLogEntries = 100
    val latencyFactor = 2
    val delayMillis = props.metricsIntervalMillis / totalLogEntries * latencyFactor

    val apiEntries = createLogEntries(totalLogEntries / 2, delayMillis, apiRequest)
    val reportEntries =
      createLogEntries(totalLogEntries / 2, delayMillis, reportRequest,
        apiEntries.last.timestamp * 1000 + delayMillis)
    val entries = apiEntries ++ reportEntries
    entries.map(e => collector.record(e)).sequence_.unsafeRunSync()

    eventBus.events.size should be >= 2

    val sections = eventBus.events.map(e => e.asInstanceOf[HttpMetrics]).map(m => m.resource.firstSection).toSet
    sections shouldBe Set("/api", "/report")

    val totalHits = eventBus.events.map(e => e.asInstanceOf[HttpMetrics]).map(m => m.hits).sum

    totalHits shouldBe totalLogEntries - 1
  }


  def createLogEntries(n: Int, delayInMills: Long,
                       request: Request,
                       startTimeMillis: Long = System.currentTimeMillis()): List[HttpLogEntry] = {
    @scala.annotation.tailrec
    def create(left: Int, res: ListBuffer[HttpLogEntry], nextTime: Long): List[HttpLogEntry] = {
      if (left == 0) res.toList
      else create(left - 1, res += HttpLogEntry(
        ip = "10.0.0.2",
        connOwner = "-",
        userId = "apache",
        timestamp = nextTime / 1000L, // convert to unix time
        request = request,
        status = 200,
        bytes = 1234
      ), nextTime + delayInMills)
    }

    create(n, ListBuffer.empty, startTimeMillis)
  }
}
