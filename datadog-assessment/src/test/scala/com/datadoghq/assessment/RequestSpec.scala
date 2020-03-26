package com.datadoghq.assessment

import com.datadoghq.assessment.LogEntry.HttpLogEntry._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class RequestSpec extends AnyFunSuite with Matchers {

  val ErrorMsg = "invalid request format"

  val validRequests =
    Table(
      ("value", "expected"),
      ("GET /api/user HTTP/1.0",   Right(Request(Get, Resource("/api/user", Vector("/api", "/user")), Http("1.0")))),
      ("POST /api/user HTTPS/1.0", Right(Request(Post, Resource("/api/user", Vector("/api", "/user")), Https("1.0")))),
    )

  val invalidRequest =
    Table(
      ("value", "expected"),
      ("",                        Left(ErrorMsg)),
      ("GET",                     Left(ErrorMsg)),
      ("GET /api/user",           Left(ErrorMsg)),
      ("PUT /api/user HTTP/1.0",  Left(ErrorMsg)),
      ("POST /api/ HTTPS/1.0",    Left(ErrorMsg)),
      ("POST /api/user TCP",      Left(ErrorMsg))
    )


  test("valid") {
    forAll(validRequests) { (value: String, expected: Either[String, Request]) =>
      Request(value) shouldBe expected
    }
  }

  test("invalid") {
    forAll(invalidRequest) { (value: String, res: Either[String, Request]) =>
      Request(value) shouldBe res
    }
  }
}
