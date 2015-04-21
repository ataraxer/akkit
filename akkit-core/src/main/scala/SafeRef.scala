package akkit

import akka.actor._
import akka.routing.RouterConfig

import scala.language.implicitConversions
import scala.reflect.ClassTag


case class SafeProps[T <: Actor](props: Props) extends AnyVal {
  def withDispatcher(dispatcher: String) = map { _ withDispatcher dispatcher }
  def withDeploy(deploy: Deploy) = map { _ withDeploy deploy }
  def withRouter(config: RouterConfig) = map { _ withRouter config }

  def map(f: Props => Props) = SafeProps[T](f(props))
}


object SafeProps {
  implicit def unwrap(safe: SafeProps[_]): Props = safe.props

  def apply[T <: Actor : ClassTag](creator: => T): SafeProps[T] = {
    SafeProps[T](Props(creator))
  }
}


case class SafeRef[T <: Actor](ref: ActorRef) extends AnyVal


object SafeRef {
  implicit def unwrapScalaRef(safe: SafeRef[_]): ScalaActorRef = safe.ref
  implicit def unwrapActorRef(safe: SafeRef[_]): ActorRef = safe.ref
}


// vim: set ts=2 sw=2 et:
