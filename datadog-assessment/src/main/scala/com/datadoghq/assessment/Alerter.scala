package com.datadoghq.assessment

import cats.effect.IO

trait Alerter[T <: LogEntry] {
  def record(logEntry: T): IO[Unit]
}