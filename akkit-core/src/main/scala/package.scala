import akka.actor._


package object akkit {
  implicit class SafeRefFactory(factory: ActorRefFactory) {
    def safeActorOf[T <: Actor](props: SafeProps[T], name: String): SafeRef[T] = {
      SafeRef { factory.actorOf(props, name) }
    }

    def safeActorOf[T <: Actor](props: SafeProps[T]): SafeRef[T] = {
      SafeRef { factory.actorOf(props) }
    }
  }
}


// vim: set ts=2 sw=2 et:
