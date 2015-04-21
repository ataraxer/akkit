package akkit

import akkit.testkit._
import akka.actor._


class SafeRefSpec
  extends ActorSpec("safe-ref-receive-spec")
  with UnitSpec
{
  import SafeRef._

  class EmptyActor extends Actor {
    def receive = { case _ => }
  }

  class Echoer extends Actor {
    def receive = { case msg => sender ! msg }
  }

  // used to type check `SafeProps` and `SafeRef`s
  def check[T <: Actor](props: SafeProps[T]): Unit = {}
  def check[T <: Actor](ref: SafeRef[T]): Unit = {}


  "SafeProps" should "preserve actor type" in {
    class Foo extends EmptyActor
    class Bar extends EmptyActor

    val fooProps = SafeProps { new Foo }
    val barProps = SafeProps { new Bar }

    check[Foo](fooProps)
    check[Bar](barProps)
  }


  it should "transparently work as a `Props`" in {
    system actorOf SafeProps { new EmptyActor }
  }


  it should "preserve type on `withDispatcher`" in {
    val safeProps = SafeProps { new EmptyActor }
    val safePropsWithDispatcher = safeProps.withDispatcher("test")

    check[EmptyActor](safeProps)
    check[EmptyActor](safePropsWithDispatcher)
  }


  "SafeRef" should "preserve actor type" in {
    val actor = system actorOf Props { new EmptyActor }
    val safeRef = SafeRef[EmptyActor](actor)
    check[EmptyActor](safeRef)
  }


  it should "transparently work as an `ActorRef`" in new Kit {
    val actor = system actorOf Props { new Echoer }
    val safeRef = SafeRef[Echoer](actor)
    safeRef ! "ping"
    expectMsg("ping")
  }


  "SafeRefFactory" should "return `SafeRef` on actor creation" in new Kit {
    val safeProps = SafeProps { new EmptyActor }
    val safeRef = system.safeActorOf(safeProps, name = "foo")
    check[EmptyActor](safeRef)
    safeRef.path.name should be ("foo")
  }


  it should "only work with `SafeProps`" in new Kit {
    """
    system safeActorOf Props { new EmptyActor }
    """ shouldNot compile
  }
}


// vim: set ts=2 sw=2 et:
