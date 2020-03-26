package com.datadoghq.assessment

import cats.effect.IO
import com.datadoghq.assessment.EventBus.Subscriber

import scala.collection.mutable.ListBuffer

object TestUtils {

  class TestEventBus extends EventBus {

    private[this] val _events = ListBuffer.empty[Event]

    override def start(): IO[Unit] =
      IO.raiseError(new UnsupportedOperationException("subscribe isn't supported"))

    override def publish(event: Event): IO[Unit] = IO {
      _events += event
    }

    def events: List[Event] = _events.toList

    override def subscribe(s: Subscriber): IO[Unit] =
      IO.raiseError(new UnsupportedOperationException("subscribe isn't supported"))
  }

}
