package io.citrine.assessment

import scala.collection.mutable.ListBuffer

object Tokenizer {


  def tokenize(source: String): Seq[Token] = {
    val tokens = ListBuffer[Token]()

    val buf = new StringBuilder()

    for (ch <- source.toCharArray) {
      if (Character.isLetter(ch)) {
        buf.append(ch)
      } else {
        if (buf.nonEmpty) {
          tokens += Token(TokenType.UNIT, buf.toString())
          buf.clear()
        }
        ch match {
          case '*' => tokens += Token(TokenType.MULT, String.valueOf(ch))
          case '/' => tokens += Token(TokenType.DEV, String.valueOf(ch))
          case '(' => tokens += Token(TokenType.LPAREN, String.valueOf(ch))
          case ')' => tokens += Token(TokenType.RPAREN, String.valueOf(ch))
          case _ => throw new RuntimeException("unsupported character")
        }


      }
    }
    if (buf.nonEmpty) {
      tokens += Token(TokenType.UNIT, buf.toString())
    }
    tokens
  }

  // Token type
  object TokenType extends Enumeration {
    val UNIT, MULT, DEV, LPAREN, RPAREN = Value
  }

  case class Token(tokenType: TokenType.Value, value: String) {
    override def toString: String = value
  }


}
