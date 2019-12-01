package io.citrine.assessment

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class InterpreterSpec extends FunSuite {

  val equations = Table(
    ("equation", "result"),
    ("degree/minute", Math.PI / 180.0 * (1 / 60.0)),
    ("degree*minute", Math.PI / 180 * 60),
    ("(degree/(minute*hectare))", (Math.PI / 180.0) * 1 / (60 * 10000))
  )


  forAll(equations) { (exp: String, result: Double) =>
    val tokens = Tokenizer.tokenize(exp)
    val parser = new Parser(tokens)
    val ast = parser.parse()
    val actual = Interpreter.interpret(ast)
    actual shouldBe result
  }

}
