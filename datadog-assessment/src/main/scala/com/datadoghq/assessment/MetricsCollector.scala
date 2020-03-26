package com.datadoghq.assessment

import cats.effect.IO

trait MetricsCollector[LE <: LogEntry, M <: Metrics] {
  def record(logEntry: LE): IO[Unit]
}