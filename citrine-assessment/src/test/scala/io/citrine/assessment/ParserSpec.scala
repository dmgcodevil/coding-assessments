package io.citrine.assessment

import io.citrine.assessment.Parser._
import io.citrine.assessment.Tokenizer.{Token, TokenType}
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParserSpec extends FunSuite {

  val equations =
    Table(
      ("equation", "ast"),
      // (degree/(minute*hectare))
      ("degree/minute*hectare", BinOp(
        BinOp(SiValue("°", SiValues("°")), Token(TokenType.DEV, "/"), SiValue("min", SiValues("min"))),
        Token(TokenType.MULT, "*"),
        SiValue("ha", SiValues("ha")))),

      ("degree/(minute*hectare)", BinOp(
        SiValue("°", SiValues("°")),
        Token(TokenType.DEV, "/"),
        BinOp(SiValue("min", SiValues("min")), Token(TokenType.MULT, "*"), SiValue("ha", SiValues("ha"))))),
    )

  forAll(equations) { (equation: String, result: ASTNode) =>
    val actual = new Parser(Tokenizer.tokenize(equation)).parse()
    actual shouldBe result
  }
}
