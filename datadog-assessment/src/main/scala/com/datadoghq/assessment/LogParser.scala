package com.datadoghq.assessment

import cats.syntax.either._
import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogEntry.HttpLogEntry._
import org.apache.commons.validator.routines.InetAddressValidator

trait LogParser[T <: LogEntry] {

  def parse(str: String): Either[String, T]
}

object LogParser {

  private val ipValidator = InetAddressValidator.getInstance()

  /**
   * Log parse for HTTP logs, see https://en.wikipedia.org/wiki/Common_Log_Format
   * Example: "10.0.0.2","-","apache",1549573860,"GET /api/user HTTP/1.0",200,1234
   */
  object HttpLogParser extends LogParser[LogEntry.HttpLogEntry] {

    private val NumberOfEntries = 7

    override def parse(str: String): Either[String, HttpLogEntry] = {
      val entries = str.split(",").map(s => s.replace("\"", ""))
      if (entries.length != NumberOfEntries) {
        Left(s"malformed log entry")
      } else {
        for {
          ip <- parseIp(entries(0))
          connOwner <- parseUserId(entries(1))
          userId <- parseUserId(entries(2))
          timestamp <- parseTimestamp(entries(3))
          req <- Request(entries(4))
          status <- parseStatusCode(entries(5))
          bytes <- parseResponseSize(entries(6))
        } yield HttpLogEntry(
          ip = ip,
          connOwner = connOwner,
          userId = userId,
          timestamp = timestamp,
          request = req,
          status = status,
          bytes = bytes)
      }
    }

    private[assessment] def parseStatusCode(value: String): Either[String, Int] = {
      Either.catchOnly[NumberFormatException](value.toInt)
        .leftMap(_ => "status code must be a number")
        .flatMap {
          case code if code < 100 || code > 599 => Left("status code should be in range [100, 599]") //RFC 7231
          case code => Right(code)
        }
    }

    private[assessment] def parseResponseSize(value: String): Either[String, Int] = {
      Either.catchOnly[NumberFormatException](value.toInt)
        .leftMap(_ => "response size must be a number")
        .flatMap {
          case size if size < 0 => Left("response size must be a positive number")
          case size => Right(size)
        }
    }

    private[assessment] def parseUserId(value: String): Either[String, String] = {
      val userId = value.trim
      if (userId.isEmpty) Left("userId cannot be empty")
      else Right(userId)
    }

    private[assessment] def parseIp(value: String): Either[String, String] = {
      if (ipValidator.isValidInet4Address(value)) Right(value)
      else Left("invalid ip address")
    }

    private[assessment] def parseTimestamp(value: String): Either[String, Long] = {
      Either.catchOnly[NumberFormatException](value.toLong)
        .leftMap(_ => "timestamp must be a number")
        .flatMap {
          case ts if ts < 0 => Left("timestamp must be a positive number")
          case ts => Right(ts)
        }
    }
  }

}
