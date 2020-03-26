package com.datadoghq.assessment

import cats.effect.IO

trait Reporter extends (Event => IO[Unit])

object Reporter {

  object ConsoleReporter extends Reporter {
    override def apply(e: Event): IO[Unit] = IO(println(e.show))
  }

}

