package com.ataraxer.akkit

import akka.actor.Actor
import akka.actor.Actor.Receive


/**
 * Allows to compose actor behaviour by stacking up `receive` methods
 * via `stackReceive`.
 *
 * {{{
 * trait Foo extends ComposableActor {
 *   stackReceive {
 *     case "foo" => println("foo")
 *   }
 * }
 *
 * trait Bar extends ComposableActor {
 *   stackReceive {
 *     case "bar" => println("bar")
 *   }
 * }
 * }}}
 *
 * class FooBar extends Foo with Bar

 * val fooBar = system actorOf Props { new FooBar }
 * fooBar ! "foo"  // outputs "foo"
 * fooBar ! "bar"  // outputs "bar"
 */
trait ComposableActor extends Actor with ComposableReceive


/**
 * Allows to compose actor behaviour by stacking up `receive` methods
 * via `stackReceive`.
 *
 * Refer to [[ComposableActor]]
 */
trait ComposableReceive { this: Actor =>
  import ComposableReceive._

  /**
   * Extendable receive functions list.
   */
  private var receiversList = List.empty[PartialFunction[Any, Unit]]

  /**
   * Registers new receiving partial function, that will extend existing.
   */
  def stackReceive(receiver: Receive): Unit = {
    receiversList = receiver :: receiversList
  }

  /**
   * Actor `receive` method composed of registered partial functions.
   */
  def receive = receiversList reduceOption { _ orElse _ } getOrElse EmptyReceive
}


object ComposableReceive {
  /**
   * Neutral receive -- ignores all messages.
   */
  val EmptyReceive: Receive = { case _ => }
}


// vim: set ts=2 sw=2 et:
