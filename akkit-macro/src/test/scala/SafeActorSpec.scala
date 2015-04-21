package akkit

import akkit.testkit._
import akkit.SafeRef._

import akka.actor._


class SafeActorMacroSpec
  extends ActorSpec("safe-actor-macro-spec")
  with UnitSpec
{
  @safeActor
  class Dumby(msg: String) extends Actor {
    def receive = { case _ => sender ! msg }
  }


  @safeActor
  class Smarty(dumby: Dumby.Ref) extends Actor {
    dumby ! "Hey there."
    def receive = { case response => testActor ! (sender -> response) }
  }


  @safeActor
  class Pinger extends Actor {
    def receive = { case "ping" => sender ! "ping" }
  }

  @safeActor
  class Ponger extends Pinger


  "@safeActor" should "generate `props` method" in {
    Dumby.props("test") shouldBe a [SafeProps[_]]
  }


  it should "fail at compile time on wrong `props` arguments" in {
    """Dumby.props(42)""" shouldNot typeCheck
    """Dumby.props()""" shouldNot typeCheck
    """Dumby.props("test", "me")""" shouldNot typeCheck
  }


  it should "generate `apply` method" in {
    Dumby("test") shouldBe a [SafeRef[_]]
  }


  it should "fail at compile time on wrong `apply` arguments" in {
    """Dumby(42)""" shouldNot typeCheck
    """Dumby()""" shouldNot typeCheck
    """Dumby("test", "me")""" shouldNot typeCheck
  }


  it should "generate `Ref` type alias" in new Kit {
    def ping(pinger: Pinger.Ref) = pinger ! "ping"
    ping(Pinger())
    """ping(Ponger())""" shouldNot typeCheck
  }


  it should "work out `Ref` ambiguity" in new Kit {
    import Pinger._
    import Ponger._

    "def ping(pinger: Ref) = {}" shouldNot compile

    def ping(pinger: PingerRef) = pinger ! "ping"  // compiles
    def pong(ponger: PongerRef) = ponger ! "pong"  // compiles

    val pinger = Pinger()
    val ponger = Ponger()

    ping(pinger)
    pong(ponger)

    """ping(ponger)""" shouldNot typeCheck
    """pong(pinger)""" shouldNot typeCheck
  }


  it should "generate `Props` with specified default dispatcher" in {
    @safeActor("foo-dispatcher")
    class Foo extends Pinger

    Foo.props().dispatcher should be ("foo-dispatcher")
  }


  it should "be awesome" in {
    val dumby = Dumby("duh")
    val smarty = Smarty(dumby)
    expectMsg(dumby.ref -> "duh")

    val namedDumby = system.safeActorOf(
      Dumby.props("wut"),
      name = "Daniel")

    Smarty(namedDumby)

    val (dumbRef, "wut") = expectMsgType[(ActorRef, String)]
    dumbRef.path.name should be ("Daniel")
  }
}


// vim: set ts=2 sw=2 et sts=2:
