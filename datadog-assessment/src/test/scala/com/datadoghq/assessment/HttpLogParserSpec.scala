package com.datadoghq.assessment

import com.datadoghq.assessment.LogEntry.HttpLogEntry
import com.datadoghq.assessment.LogEntry.HttpLogEntry.{Get, Http, Post, Request, Resource}
import com.datadoghq.assessment.LogParser.HttpLogParser
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class HttpLogParserSpec extends AnyFunSuite with Matchers {

  val validLog =
    Table(
      ("value", "expected"),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,1234",
        Right(HttpLogEntry(
          ip = "10.0.0.2",
          connOwner = "-",
          userId = "apache",
          timestamp = 1549573860,
          request = Request(Get, Resource("/api/user", Vector("/api", "/user")), Http("1.0")),
          status = 200,
          bytes = 1234
        ))),
      ("\"10.0.0.5\",\"-\",\"apache\",1549573863,\"POST /report HTTP/1.0\",200,1307",
        Right(HttpLogEntry(
          ip = "10.0.0.5",
          connOwner = "-",
          userId = "apache",
          timestamp = 1549573863,
          request = Request(Post, Resource("/report", Vector("/report")), Http("1.0")),
          status = 200,
          bytes = 1307
        )))
    )

  val invalidLog =
    Table(
      ("value", "expected"),
      ("\"10.0.0\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,1234",      Left("invalid ip address")),
      ("\"10.0.0.2\",\"\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,1234",     Left("userId cannot be empty")),
      ("\"10.0.0.2\",\"-\",\"\",1549573860,\"GET /api/user HTTP/1.0\",200,1234",          Left("userId cannot be empty")),
      ("\"10.0.0.2\",\"-\",\"apache\",abc,\"GET /api/user HTTP/1.0\",200,1234",           Left("timestamp must be a number")),
      ("\"10.0.0.2\",\"-\",\"apache\",-1,\"GET /api/user HTTP/1.0\",200,1234",            Left("timestamp must be a positive number")),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",abc,1234",    Left("status code must be a number")),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",99,1234",     Left("status code should be in range [100, 599]")),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",600,1234",    Left("status code should be in range [100, 599]")),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,abc",     Left("response size must be a number")),
      ("\"10.0.0.2\",\"-\",\"apache\",1549573860,\"GET /api/user HTTP/1.0\",200,-1",      Left("response size must be a positive number")),
    )


  test("valid") {
    forAll(validLog) { (value: String, expected: Either[String, HttpLogEntry]) =>
      HttpLogParser.parse(value) shouldBe expected
    }
  }

  test("invalidLog") {
    forAll(invalidLog) { (value: String, expected: Either[String, HttpLogEntry]) =>
      HttpLogParser.parse(value) shouldBe expected
    }
  }
}
