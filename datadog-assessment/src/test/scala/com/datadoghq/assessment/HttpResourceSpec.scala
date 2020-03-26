package com.datadoghq.assessment

import com.datadoghq.assessment.LogEntry.HttpLogEntry.Resource
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class HttpResourceSpec extends AnyFunSuite with Matchers {
  val validResources =
    Table(
      ("value", "expected"),
      ("/a",   Right(Resource("/a", Vector("/a")))),
      ("/a/b", Right(Resource("/a/b", Vector("/a", "/b"))))
    )

  val invalidResources =
    Table(
      ("value", "expected"),
      ("",    Left("resource path has invalid format")),
      ("/",   Left("resource path has invalid format")),
      ("//",  Left("resource path has invalid format")),
      ("/a/", Left("resource path has invalid format"))
    )

  test("valid") {
    forAll(validResources) { (value: String, expected: Either[String, Resource]) =>
      Resource(value) shouldBe expected
    }
  }

  test("invalid") {
    forAll(invalidResources) { (value: String, expected: Either[String, Resource]) =>
      Resource(value) shouldBe expected
    }
  }
}
