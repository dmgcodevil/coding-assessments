package com.datadoghq.assessment

import com.datadoghq.assessment.LogEntry.HttpLogEntry.{Http, Https, Protocol}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.{forAll, _}

class ProtocolSpec extends AnyFunSuite with Matchers {

  val ErrorMsg = "invalid protocol format"

  val validProtocols =
    Table(
      ("value", "expected"),
      ("HTTP/1.0",  Right(Http("1.0"))),
      ("http/1.0",  Right(Http("1.0"))),
      ("HTTPS/1.0", Right(Https("1.0"))),
    )

  val invalidProtocols =
    Table(
      ("value", "expected"),
      ("UDP",     Left(ErrorMsg)),
      ("UDP/",    Left(ErrorMsg)),
      ("UDP/1",   Left(ErrorMsg)),
      ("UDP/1.",  Left(ErrorMsg)),
      ("UDP/a.b", Left(ErrorMsg))
    )

  test("valid") {
    forAll(validProtocols) { (value: String, expected: Either[String, Protocol]) =>
      Protocol(value) shouldBe expected
    }
  }

  test("invalid") {
    forAll(invalidProtocols) { (value: String, expected: Either[String, Protocol]) =>
      Protocol(value) shouldBe expected
    }
  }
}
