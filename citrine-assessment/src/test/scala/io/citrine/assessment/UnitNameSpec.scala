package io.citrine.assessment

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class UnitNameSpec extends FunSuite {
  val units = Table(
    ("equation", "result"),
    ("degree", "rad"),
    ("degree/minute", "rad/s"),
    ("(degree/(minute*hectare))", "(rad/(s*m^2))")
  )

  forAll(units) { (source: String, result: String) =>
    val tokens = Tokenizer.tokenize(source)
    val actual = Units.unitName(tokens)
    actual shouldBe result
  }
}
