package io.citrine.assessment

import io.citrine.assessment.Tokenizer.{Token, TokenType}

import scala.collection.mutable

object Units {

  val NameToSymbol: Map[String, String] = Map(
    // time
    "minute" -> "min",
    "hour" -> "h",
    "day" -> "d",
    // unitless/plane angle
    "degree" -> "°",
    "arcminute" -> "'",
    "arcsecond" -> "\"",
    //area
    "hectare" -> "ha",
    // volume
    "litre" -> "L",
    //mass
    "tonne" -> "t"
  )

  val SymbolToSiUnit: Map[String, String] = Map(
    // time
    "min" -> "s",
    "h" -> "s",
    "d" -> "s",
    // unitless/plane angle
    "°" -> "rad",
    "'" -> "rad",
    "\"" -> "rad",
    //area
    "ha" -> "m^2",
    //  volume
    "L" -> "m^3",
    // mass
    "t" -> "kg"
  )

  val Symbols: Set[String] = SymbolToSiUnit.keySet


  def unitName(tokens: Seq[Token]): String = {
    val res = new mutable.StringBuilder()

    for (token <- tokens) {
      token match {
        case Token(TokenType.UNIT, value) => {
          var symbol = value
          if (!Symbols.contains(value)) {
            symbol = NameToSymbol(value)
          }
          res.append(SymbolToSiUnit(symbol))
        }
        case Token(_, value) => res.append(value)

      }
    }
    res.toString()
  }

}