package com.datadoghq.assessment

trait Alert extends Event

object Alert {

  sealed trait Status

  object On extends Status

  object Off extends Status

  case class HttpAlert(section: String, status: Alert.Status, timestamp: Long, hits: Int, totalSize: Int) extends Alert {
    override def show: String = status match {
      case On => s"Alert[High traffic]: $section,$timestamp,$hits,$totalSize"
      case Off => s"Alert[High traffic]-Recovered: $section,$timestamp,$hits,$totalSize"
    }
  }

}