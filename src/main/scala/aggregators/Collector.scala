package com.ataraxer.akkit.aggregator

import akka.actor.{Actor, ActorRefFactory, ActorRef, Props}


object CollectorSpawner {
  import Collector._

  private def spawn(actor: => Actor)(implicit context: ActorRefFactory) =
    context.actorOf(Props(actor))

  class Counter(expectedSize: Int)(implicit context: ActorRefFactory) {
    /**
     * Spawn [[Collector]] actor that collects specified objects until expected
     * count is exceeded.
     */
    def apply(objects: Any*)(implicit client: ActorRef) = spawn {
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = objects contains msg
      }
    }

    /**
     * Spawn [[Collector]] actor that collects objects that match provided partial
     * function until expected count is exceeded.
     */
    def matching(extractor: Extractor)(implicit client: ActorRef) = spawn {
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    }
  }
}


trait CollectorSpawner {
  import CollectorSpawner._
  import Collector._

  implicit val context: ActorRefFactory

  object collect {
    /**
     * Spawn [[Collector]] actor that collects specified objects.
     */
    def apply(objects: Any*) = spawn {
      new Collector {
        def expected(msg: Any) = objects contains msg
      }
    }

    /**
     * Spawn [[Collector]] actor that collects objects that match provided partial
     * function.
     */
    def matching(extractor: Extractor) = spawn {
      new Collector {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    }

    /**
     * Returns a builder object that can be applied to variable number of objects
     * or a partial function to produce new [[Collector]] with counter.
     */
    def count(expectedSize: Int) = new Counter(expectedSize)
  }
}


object Collector {
  type Extractor = PartialFunction[Any, Any]
  case class Collection(result: Seq[Any])
  case object Flush
  case object Peak
}


abstract class Collector(
  expectedSize: Option[Int] = None,
  client: Option[ActorRef]  = None)
    extends Actor
{
  import Collector._

  private var result = Seq.empty[Any]

  def expected(msg: Any): Boolean
  def extract(msg: Any): Any = msg

  def isDone = expectedSize match {
    case Some(size) => result.size == size
    case None => false
  }

  def receive = {
    case Flush => {
      sender ! Collection(result)
      result = Seq.empty[Any]
    }

    case Peak => sender ! Collection(result)

    case msg if expected(msg) => {
      result :+= extract(msg)
      if (isDone) {
        client map { _ ! Collection(result) }
        context.stop(self)
      }
    }
  }
}


// vim: set ts=2 sw=2 et:
