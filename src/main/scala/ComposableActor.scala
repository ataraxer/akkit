package com.ataraxer.akkit

import akka.actor.Actor
import akka.actor.Actor.Receive


/**
 * ComposableActor trait allows to compose actors via new partial function
 * registratino method, will which extend existing.
 */
trait ComposableActor extends Actor {
  /**
   * Extendable receive-functions list.
   */
  private var receiversList = List.empty[Receive]

  /**
   * Registers new receiving partial function, that will extend existing.
   */
  def addReceiver(receiver: Receive): Unit = {
    receiversList = receiver :: receiversList
  }

  /**
   * Actor `receive` method composed of registered partial functions.
   */
  def receive = receiversList reduce { _ orElse _ }
}


// vim: set ts=2 sw=2 et:
