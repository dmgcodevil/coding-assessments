package io.citrine.assessment

import io.citrine.assessment.Parser._
import io.citrine.assessment.Tokenizer.{Token, TokenType}

import scala.collection.mutable.ListBuffer

/*
( a * b / c ) => ( (a * b) / c)
* tree:
*     [/]
*    /   \
*   [*]  [c]
*   / \
* [a] [b]

tree:
* ( a * (b / c) )
*    [*]
*    /  \
*   [a] [/]
*       /  \
*      [b] [c]
*
* operators with higher precedence end up being lower in the tree
*/

class Parser(tokens0: Seq[Token]) {

  require(tokens0.nonEmpty)
  val ans =  ListBuffer(1)
  private val tokens = tokens0.to[ListBuffer]
  private var currentToken = tokens.remove(0)

  // compare the given `tokenType` with the current token,
  // if they match then get the next token and set it as current
  // otherwise throw an exception
  def consume(tokenType: TokenType.Value): Unit = {
    if (tokens.nonEmpty) {
      if (currentToken.tokenType == tokenType) {
        currentToken = tokens.remove(0)
      } else throw new IllegalStateException("invalid token")
    }
    // no more tokens

  }


  // factor can be: UNIT | LPAREN
  def factor(): ASTNode = {
    val token = currentToken
    if (token.tokenType == TokenType.UNIT) {
      consume(TokenType.UNIT)
      var symbol = token.value
      if (!Units.Symbols.contains(symbol)) {
        symbol = Units.NameToSymbol(symbol)
      }
      SiValue(symbol, SiValues(symbol))

    } else if (token.tokenType == TokenType.LPAREN) {
      consume(TokenType.LPAREN)
      val node = exp()
      consume(TokenType.RPAREN)
      node
    } else throw new IllegalStateException("expected token types: UNIT | LPAREN")
  }

  // factor (MULT or DIV) factor
  // an expression can start with either UNIT(degree, minute, ...) or LPAREN('(')
  def exp(): ASTNode = {
    var node = factor()

    while (Set(TokenType.MULT, TokenType.DEV).contains(currentToken.tokenType)) {
      val token = currentToken
      token.tokenType match {
        case TokenType.MULT => consume(TokenType.MULT)
        case TokenType.DEV => consume(TokenType.DEV)
      }
      node = BinOp(left = node, op = token, right = factor())
    }
    node
  }


  def parse(): ASTNode = {
    exp()
  }

}

object Parser {

  val SiValues = Map(
    // time
    "min" -> 60.0,
    "h" -> 3600.0,
    "d" -> 86400.0,
    // unitless/plane angle
    "°" -> Math.PI / 180.0,
    "'" -> Math.PI / 10800.0,
    "\"" -> Math.PI / 648000.0,
    //area
    "ha" -> 10000.0,
    //  volume
    "L" -> 0.001,
    // mass
    "t" -> 1000.0
  )

  trait ASTNode

  case class BinOp(left: ASTNode, op: Token, right: ASTNode) extends ASTNode

  case class SiValue(symbol: String, value: Double) extends ASTNode

}