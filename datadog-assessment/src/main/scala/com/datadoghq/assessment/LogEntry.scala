package com.datadoghq.assessment


sealed trait LogEntry

object LogEntry {

  import LogEntry.HttpLogEntry._

  /**
   * Http metrics.
   *
   * @param ip        the IP address of the client (remote host) which made the request to the server
   * @param connOwner the identity of a user of this TCP connection
   * @param userId    the userid of the person that sent a request
   * @param timestamp the date in UNIX time format when request was received
   * @param request   http request
   * @param status    request status
   * @param bytes     response size in bytes
   */
  case class HttpLogEntry(ip: String,
                          connOwner: String,
                          userId: String,
                          timestamp: Long,
                          request: Request,
                          status: Int,
                          bytes: Int) extends LogEntry

  object HttpLogEntry {

    sealed trait HttpMethod extends Show {
      override def toString: String = show
    }

    object Get extends HttpMethod {
      override def show: String = "GET"
    }

    object Post extends HttpMethod {
      override def show: String = "POST"
    }

    object HttpMethod {
      def apply(value: String): Either[String, HttpMethod] = {
        value.toUpperCase match {
          case "GET" =>  Right(Get)
          case "POST" => Right(Post)
        }
      }
    }

    sealed trait Protocol extends Show {
      val version: String

      override def toString: String = show
    }

    case class Http(version: String) extends Protocol {
      override def show: String = s"HTTP/$version"
    }

    case class Https(version: String) extends Protocol {
      override def show: String = s"HTTPS/$version"
    }

    object Protocol {
      private[assessment] val regex = """(?i)(HTTP|HTTPS)/(\d+.\d+)""".r

      def apply(value: String): Either[String, Protocol] = {
        value match {
          case regex(p, v) => p.toUpperCase match {
            case "HTTP"  => Right(Http(v))
            case "HTTPS" => Right(Https(v))
          }
          case _         => Left("invalid protocol format")
        }
      }
    }


    case class Resource(value: String, sections: Vector[String]) extends Show {
      val firstSection: String = sections.head

      override def show: String = value

      override def toString: String = show
    }

    object Resource {

      private[assessment] val regex = """(/[^/]+)+?""".r

      def apply(value: String): Either[String, Resource] = {
        value match {
          case regex(_) => Right(Resource(value, regex.findAllMatchIn(value).map(m => m.matched).toVector))
          case _        => Left("resource path has invalid format")
        }
      }
    }

    case class Request(method: HttpMethod, resource: Resource, protocol: Protocol) extends Show {
      override def show: String = s"$method $resource $protocol"

      override def toString: String = show
    }

    object Request {

      // regexp to extract main groups
      private[assessment] val regex = """(?i)(GET|POST)\s((?:/[^/]+)+)\s((?i)(?:HTTP|HTTPS)/\d+.\d+)""".r

      def apply(value: String): Either[String, Request] = {
        value match {
          case regex(methodGroup, resourceGroup, protocolGroup) =>
            for {
              method <- HttpMethod(methodGroup)
              resource <- Resource(resourceGroup)
              protocol <- Protocol(protocolGroup)
            } yield Request(method, resource, protocol)
          case _ => Left("invalid request format")
        }
      }
    }

  }

}


