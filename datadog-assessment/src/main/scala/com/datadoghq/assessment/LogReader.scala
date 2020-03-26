package com.datadoghq.assessment

import java.io.{BufferedReader, File, FileReader, InputStreamReader}

import cats.effect.{IO, Timer}
import cats.syntax.flatMap._

import scala.concurrent.duration.{FiniteDuration, _}

trait LogReader {

  def start(handle: String => IO[Unit])(implicit timer: Timer[IO]): IO[Unit]

}

object LogReader {

  class BufferedLogReader(buf: BufferedReader, pollInterval: FiniteDuration) extends LogReader {
    override def start(handle: String => IO[Unit])(implicit timer: Timer[IO]): IO[Unit] = {

      def read: IO[Unit] = {
        for {
          line <- IO(buf.readLine())
          _ <- if (line == null) IO.sleep(pollInterval) else handle(line)
          _ <- read
        } yield ()
      }

      IO(buf.readLine()) >> // read header
        read
    }
  }

  def console(pollInterval: FiniteDuration = 1000.millis): LogReader =
    new BufferedLogReader(new BufferedReader(new InputStreamReader(System.in)), pollInterval)

  def create(file: File, pollInterval: FiniteDuration = 1000.millis): LogReader = {
    val reader = new FileReader(file)
    val buf = new BufferedReader(reader)
    new BufferedLogReader(buf, pollInterval)
  }

}

