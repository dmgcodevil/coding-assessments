package io.citrine.assessment

import io.citrine.assessment.Parser._
import io.citrine.assessment.Tokenizer._

object Interpreter {

  def interpret(ast: ASTNode): Double = {

    def step(node: ASTNode): Double = {
      node match {
        case SiValue(_, value) => value
        case BinOp(first, op, second) =>
          op match {
            case Token(TokenType.MULT, _) => step(first) * step(second)
            case Token(TokenType.DEV, _) => step(first) * (1 / step(second))
            case Token(tokenType, _) => throw new RuntimeException(s"unsupported operator: $tokenType")
          }
      }
    }

    step(ast)

  }

}
