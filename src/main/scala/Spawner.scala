package com.ataraxer.akkit

import akka.actor._
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


    object actor {
      /**
       * Creates stateless actor with provided behaviour.
       *
       * @param behaviour Actor's behaviour.
       * @return Reference to the created actor.
       */
      def apply(behaviour: Receive)(implicit factory: ActorRefFactory) = {
        spawn { new Actor {
          def receive = behaviour
        }}
      }

      /**
       * Creates a stateless actor which can access it's context.
       */
      def withContext
        (behaviour: ActorContext => Receive)
        (implicit factory: ActorRefFactory) =
      {
        spawn { new Actor { def receive = {
          case message => behaviour(context)(message)
        }}}
      }
    }


    object adapter {
      /**
       * Creates a transient actor which will receive a message, transform it
       * if applicable and forward the result back to it's creator.
       */
      def apply
        (adapt: PartialFunction[Any, Any])
        (implicit factory: ActorRefFactory, client: ActorRef) =
      {
        adapter.to(client)(adapt)
      }

      /**
       * Creates a transient actor which will receive a message, transform it
       * if applicable and forward the result back to specified responder.
       */
      def to
        (client: ActorRef)
        (adapt: PartialFunction[Any, Any])
        (implicit factory: ActorRefFactory) =
      {
        handler withContext { implicit context => {
          case message => client forward {
            if (adapt isDefinedAt message) adapt(message)
            else message
          }
        }}
      }
    }


    object handler {
      /**
       * Creates stateless actor with provided behaviour, that will handle
       * only one message that he can handle and self-destruct.
       *
       * @param reaction Actor's behaviour.
       * @return Reference to the created actor.
       */
      def apply(reaction: Receive)(implicit factory: ActorRefFactory) = {
        handler withContext { context => reaction }
      }

      /**
       * Creates context-aware actor with provided behaviour, that will handle
       * only one message that he can handle and self-destruct.
       */
      def withContext
        (reaction: ActorContext => Receive)
        (implicit factory: ActorRefFactory) =
      {
        actor withContext { context => { case message =>
          val receive = reaction(context)
          if (receive isDefinedAt message) receive(message)
          context.stop(context.self)
        }}
      }
    }
  }
}


// vim: set ts=2 sw=2 et:
