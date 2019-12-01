package io.citrine.assessment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.citrine.assessment.endpoint.UnitsEndpoint

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App with UnitsEndpoint {

  implicit val sys: ActorSystem = ActorSystem("akka-http-app")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = sys.dispatcher

  val log = sys.log

  Http().bindAndHandle(unitsRoute, "0.0.0.0", 8080).onComplete {
    case Success(b) => log.info(s"application is up and running at ${b.localAddress.getHostName}:${b.localAddress.getPort}")
    case Failure(e) => log.error(s"could not start application: {}", e.getMessage)
  }
}
