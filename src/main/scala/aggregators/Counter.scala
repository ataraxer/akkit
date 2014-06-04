package com.ataraxer.akkit.aggregator

import akka.actor.{Actor, ActorRefFactory, ActorRef, Props}
import scala.collection.mutable


object Counter {
  type CountFilter = PartialFunction[Any, Boolean]
  val defaultPF: CountFilter = { case _ => false }

  case object Done
  case class Count(result: Int)
  case class DistinctCount(result: Map[Any, Int])

  case object Flush
  case object Peak
}


trait CounterSpawner {
  import Counter._

  val context: ActorRefFactory

  private def spawn(actor: => Actor) =
    context.actorOf(Props(actor))

  object count {

    /**
     * Spawn Counter actor that counts specified objects.
     */
    def apply(objects: Any*) = {
      spawn { new Counter {
        def expected(msg: Any) = objects contains msg
      }}
    }

    /**
     * Spawn Counter actor that counts objects that match provided partial
     * function.
     */
    def withPF(expect: CountFilter) = {
      spawn { new Counter {
        def expected(msg: Any) = (expect orElse defaultPF)(msg)
      }}
    }

    /**
     * Spawn Counter actor that counts specified objects until expected count
     * is exceeded.
     */
    def expect(expectedSize: Int)(objects: Any*)
              (implicit client: ActorRef) = {
      spawn {
        new Counter(client = Some(client), expectedSize = Some(expectedSize)) {
          def expected(msg: Any) = objects contains msg
        }
      }
    }

    /**
     * Spawn Counter actor that counts objects that match provided partial
     * function until expected count is exceeded.
     */
    def expectWithPF(expectedSize: Int)(expect: CountFilter)
                    (implicit client: ActorRef) = {
      spawn {
        new Counter(client = Some(client), expectedSize = Some(expectedSize)) {
          def expected(msg: Any) = (expect orElse defaultPF)(msg)
        }
      }
    }

    /**
     * Spawn Counter actor that counts dictinct number of specified objects.
     */
    def distinct(objects: Any*) = {
      spawn { new Counter(distinct = true) {
        def expected(msg: Any) = objects contains msg
      }}
    }

    /**
     * Spawn Counter actor that counts distinct number of objects that match
     * provided partial function.
     */
    def distinctWithPF(expect: CountFilter) = {
      spawn { new Counter(distinct = true) {
        def expected(msg: Any) = (expect orElse defaultPF)(msg)
      }}
    }

  }
}


abstract class Counter(
  distinct: Boolean = false,
  expectedSize: Option[Int] = None,
  client: Option[ActorRef] = None)
    extends Actor
{
  import Counter._

  private val result = mutable.Map.empty[Any, Int].withDefaultValue(0)

  def expected(msg: Any): Boolean

  def isDone = expectedSize match {
    case Some(size) => result.map(_._2).sum == size
    case None => false
  }

  private def response = distinct match {
    case true  => DistinctCount(result.toMap)
    case false => Count(result.values.sum)
  }

  def receive = {
    case Flush => {
      sender ! response
      result.clear()
    }

    case Peak => sender ! response

    case msg if expected(msg) => {
      result(msg) += 1
      if (isDone) {
        client map { _ ! Done }
        context.stop(self)
      }
    }
  }
}


// vim: set ts=2 sw=2 et:
