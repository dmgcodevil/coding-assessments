package io.citrine.assessment.endpoint

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.citrine.assessment.endpoint.json.JsonSupport
import io.citrine.assessment.{Interpreter, Parser, Tokenizer, Units}

trait UnitsEndpoint extends JsonSupport {

  implicit val mat: Materializer

  val unitsRoute: Route = {
    get {
      path("units" / "si") {
        parameters('units.as[String]) { units =>
          val tokens = Tokenizer.tokenize(units)
          val parser = new Parser(tokens)
          val ast = parser.parse()
          val res = Interpreter.interpret(ast)
          val unitName = Units.unitName(tokens)
          complete(Result(unitName, res))
        }
      }
    }
  }
}
