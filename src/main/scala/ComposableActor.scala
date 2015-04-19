package com.ataraxer.akkit

import akka.actor.Actor
import akka.actor.Actor.Receive


/**
 * ComposableActor trait allows to compose actors via new partial function
 * registratino method, will which extend existing.
 */
trait ComposableActor extends Actor {
  import ComposableActor._

  /**
   * Extendable receive functions list.
   */
  private var receiversList = List.empty[PartialFunction[Any, Unit]]

  /**
   * Registers new receiving partial function, that will extend existing.
   */
  def addReceiver(receiver: Receive): Unit = {
    receiversList = receiver :: receiversList
  }

  /**
   * Actor `receive` method composed of registered partial functions.
   */
  def receive = receiversList.foldLeft(EmptyReceive) { _ orElse _ }
}


object ComposableActor {
  val EmptyReceive: Receive = { case _ => }
}


// vim: set ts=2 sw=2 et:
