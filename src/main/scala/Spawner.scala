package com.ataraxer.akkit

import akka.actor.{Actor, ActorRefFactory, Props}
import akka.actor.Actor.Receive

import scala.reflect.ClassTag


/**
 * Spawner trait defines methods for creation of simple stateless actors.
 */
trait Spawner {
  object spawn {
    /**
     * Creates provided actor without constructor arguments and name.
     */
    def apply(actor: => Actor)(implicit factory: ActorRefFactory) =
      factory.actorOf(Props(actor))

    def apply[T <: Actor : ClassTag](implicit factory: ActorRefFactory) =
      factory.actorOf(Props[T])

    /**
     * Creates stateless actor with provided behaviour.
     *
     * @param behaviour Actor's behaviour.
     * @return Reference to the created actor.
     */
    def actor(behaviour: Receive)(implicit factory: ActorRefFactory) = {
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
    def handler(reaction: Receive)(implicit factory: ActorRefFactory) = {
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
