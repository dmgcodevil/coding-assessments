package io.citrine.assessment

import io.citrine.assessment.Tokenizer._
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class TokenizerSpec extends FunSuite {

  test("tokenize") {
    val units = "(degree/(minute*hectare))"
    val tokens = Tokenizer.tokenize(units)
    tokens shouldBe Seq(
      Token(TokenType.LPAREN, "("),
      Token(TokenType.UNIT, "degree"),
      Token(TokenType.DEV, "/"),
      Token(TokenType.LPAREN, "("), Token(TokenType.UNIT, "minute"),
      Token(TokenType.MULT, "*"), Token(TokenType.UNIT, "hectare"), Token(TokenType.RPAREN, ")"),
      Token(TokenType.RPAREN, ")"))
  }

}