package com.datadoghq.assessment

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Collectors

import cats.effect.{ContextShift, IO}
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.parallel._
import com.datadoghq.assessment.EventBus.Subscriber
import monix.catnap.ConcurrentQueue
import monix.execution.BufferCapacity.Unbounded

import scala.jdk.CollectionConverters._

trait EventBus {

  def start(): IO[Unit]

  def publish(event: Event): IO[Unit]

  def subscribe(s: Subscriber): IO[Unit]

}

object EventBus {

  type Subscriber = Event => IO[Unit]

  class MonixBasedEventBus(queue: ConcurrentQueue[IO, Event])(implicit ctxShift: ContextShift[IO]) extends EventBus {
    private val subscribers = new ConcurrentLinkedQueue[Subscriber]

    override def publish(event: Event): IO[Unit] = queue.offer(event)

    override def subscribe(s: Subscriber): IO[Unit] = IO {
      subscribers.offer(s)
    }

    override def start(): IO[Unit] = {
      def step: IO[Unit] =
        queue.poll.flatMap(event => {
          subscribers.stream().map(sub => sub(event)).collect(Collectors.toList[IO[Unit]]).asScala.toList.parSequence_
        }) >> step

      step
    }
  }

  object MonixBasedEventBus {
    def unbounded()(implicit ctxShift: ContextShift[IO]): IO[EventBus] = {
      for {
        queue <- ConcurrentQueue[IO].withConfig[Event](capacity = Unbounded(), channelType = monix.execution.ChannelType.SPMC)
      } yield new MonixBasedEventBus(queue)
    }
  }

}
