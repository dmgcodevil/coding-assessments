package com.datadoghq.assessment

import java.io.File

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogParser.HttpLogParser
import com.datadoghq.assessment.Monitor.HttpMonitor.Props

import scala.concurrent.duration._


object MainApp extends IOApp {


  // Default values
  val DefaultMetricsIntervalMillis = 10000
  val DefaultThresholdPerSec = 10
  val DefaultAlertIntervalMillis = 120000
  val DefaultLogPollInterval = 1000

  //  Command line arg names
  val logFilePathArg = "-f"
  val helpArg = "--help"
  val metricsIntervalMillisArg = "--metrics-interval-millis"
  val thresholdPerSecArg = "--threshold-per-sec"
  val alertIntervalMillisArg = "--alert-interval-millis"
  val logPollIntervalArg = "--log-poll-interval-millis"

  val help = Map(
    logFilePathArg -> "a path to http log file (optional). If not specified the program reads from the console",
    metricsIntervalMillisArg -> s"time duration in millis to flush collected metrics (optional). default = $DefaultMetricsIntervalMillis",
    thresholdPerSecArg -> s"max number of hits per second allowed within 'alert-interval-millis' (optional). default = $DefaultThresholdPerSec",
    alertIntervalMillisArg -> s"time duration in millis to monitor alerts (optional). default =  $DefaultAlertIntervalMillis",
    logPollIntervalArg -> s"time duration in millis to check the log file for changes (optional). default =  $DefaultLogPollInterval"
  )


  override def run(args: List[String]): IO[ExitCode] = {

    val argMap = collectArgs(args)

    val logPollInterval = argMap.getOrElse(logPollIntervalArg, DefaultLogPollInterval).asInstanceOf[Int]

    if (argMap.contains(helpArg)) {
      IO(printHelp()) >> IO.pure(ExitCode.Success)
    } else {
      for {
        props <- IO.pure(createProps(argMap))
        eventBus <- EventBus.MonixBasedEventBus.unbounded()
        _ <- eventBus.subscribe(Reporter.ConsoleReporter)
        _ <- eventBus.start().start
        monitor <- Monitor.httpMonitor(props, eventBus)
        logReader <- argMap.get(logFilePathArg) match {
          case None => IO(LogReader.console(logPollInterval.millis))
          case Some(file: String) => IO(LogReader.create(new File(file), logPollInterval.millis))
        }
        _ <- logReader.start(process(_, monitor))
      } yield ExitCode.Success
    }


  }

  def process(line: String, monitor: Monitor[HttpLogEntry]): IO[Unit] = {
    HttpLogParser.parse(line) match {
      case Right(metrics) => monitor.record(metrics)
      case _ => IO(println(s"WARN: invalid log entry: $line"))
    }

  }

  def printHelp(): Unit = {
    help.foreach {
      case (name, value) => println(s"'$name' - $value")
    }
  }

  def createProps(args: Map[String, Any]): Props = {
    Props(
      metricsIntervalMillis = args.getOrElse(metricsIntervalMillisArg, DefaultMetricsIntervalMillis).asInstanceOf[Int],
      thresholdPerSec = args.getOrElse(thresholdPerSecArg, DefaultThresholdPerSec).asInstanceOf[Int],
      alertIntervalMillis = args.getOrElse(alertIntervalMillisArg, DefaultAlertIntervalMillis).asInstanceOf[Int]
    )
  }

  def collectArgs(args: List[String]): Map[String, Any] = {
    def collectArgs(args: List[String], argsMap: Map[String, Any]): Map[String, Any] = {
      args match {
        case "--help" :: tail => collectArgs(tail, argsMap + (helpArg -> null))
        case name :: value :: tail => collectArgs(tail, argsMap + (name -> value))
        case _ => argsMap
      }
    }

    collectArgs(args, Map.empty)
  }
}
