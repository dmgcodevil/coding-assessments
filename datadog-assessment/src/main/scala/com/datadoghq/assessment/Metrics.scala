package com.datadoghq.assessment

import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogEntry.HttpLogEntry.Resource

sealed trait Metrics extends Event

object Metrics {

  class HttpMetrics(val resource: Resource) extends Metrics {
    private var _totalSize = 0
    private var _hits = 0
    private var _timestamp = 0L

    def record(logEntry: HttpLogEntry): Unit = {
      _totalSize = _totalSize + logEntry.bytes
      _hits = _hits + 1
      _timestamp = logEntry.timestamp
    }

    def totalSize: Int = _totalSize

    def hits: Int = _hits

    def timestamp: Long = _timestamp

    /**
     * Converts http metrics object to a string in the following format:
     * [the section],[the timestamp of the last recorded log entry],[the total size of traffic received in bytes],[the total number of hits]
     */
    override def show: String = s"${resource.firstSection},${_timestamp},${_totalSize},${_hits}"

    override def toString: String = show
  }

}

