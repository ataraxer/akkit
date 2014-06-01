package com.ataraxer.akkit

import akka.actor.{Actor, ActorRefFactory, Props}
import akka.actor.Actor.Receive


/**
 * Spawner trait defines methods for creation of simple stateless actors.
 */
trait Spawner {
  val context: ActorRefFactory

  object spawn {
    /**
     * Creates provided actor without constructor arguments and name.
     */
    def apply(actor: => Actor) =
      context.actorOf(Props(actor))

    /**
     * Creates stateless actor with provided behaviour.
     *
     * @param behaviour Actor's behaviour.
     * @return Reference to the created actor.
     */
    def actor(behaviour: Receive) = {
      spawn { new Actor {
        def receive = behaviour
      }}
    }

    /**
     * Creates stateless actor with provided behaviour, that will handle only one
     * message that he can handle and self-destruct.
     *
     * @param reaction Actor's behaviour.
     * @return Reference to the created actor.
     */
    def handler(reaction: Receive) = {
      spawn { new Actor { def receive = {
        case message
          if reaction.isDefinedAt(message) =>
            reaction(message)
            context.stop(self)
      }}}
    }
  }
}


// vim: set ts=2 sw=2 et:
