package akkit

import akka.testkit.{TestKit, TestActorRef, TestProbe, ImplicitSender}
import akka.actor.{ActorSystem, Actor, ActorRef}

import scala.concurrent.duration._


object SpawnerSpec {
  case object Ping
  case object Pong
  case object Done

  class Echoer extends Actor {
    def receive = {
      case message => sender ! message
    }
  }
}


class SpawnerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
    with Spawner
{
  import SpawnerSpec._

  def this() = this(ActorSystem("spawner-spec"))


  "A Spawner" should "spawn actor of provided class" in {
    spawn[Echoer] shouldBe an [ActorRef]
  }


  it should "spawn anonymous actors" in {
    val echoer = spawn[Echoer]
    val actor  = spawn.actor { case message => echoer ! Ping }
    actor ! Ping
    expectMsg(Ping)
    actor ! Ping
    expectMsg(Ping)
  }


  it should "be able to spawn anonymous one-time actors" in {
    val echoer = spawn[Echoer]
    val actor  = spawn.handler { case message => echoer ! Ping }
    val probe  = TestProbe()
    probe watch actor
    actor ! Ping
    expectMsg(Ping)
    probe expectTerminated actor
  }


  it should "spawn anonymous context-aware one-time actors" in {
    val actor = spawn.handler withContext { context => {
      case Ping => context.sender ! Pong
    }}

    val probe = TestProbe()
    probe watch actor

    actor ! Ping
    expectMsg(Pong)
    probe expectTerminated actor
  }


  it should "be able to spawn context-aware anonymous actors" in {
    val actor = spawn.actor withContext { context => {
      case message => context.sender ! Ping
    }}

    actor ! Ping
    expectMsg(Ping)
  }


  it should "spawn one-time message adapters" in {
    val adapterOne = spawn adapter { case Ping => Pong }

    adapterOne ! Ping
    expectMsg(Pong)

    val adapterTwo = spawn adapter { case Ping => Pong }
    val echoer = spawn[Echoer]
    echoer.tell(Ping, adapterTwo)
    expectMsg(Pong)
  }


  it should "spawn one-time message adapter with specified respondent" in {
    val probe = TestProbe()
    val adapterOne = spawn.adapter.to(probe.ref) {
      case Ping => Pong
    }

    adapterOne ! Ping
    probe.expectMsg(Pong)
  }


  it should "spawn one-time message routers" in {
    val pinger = TestProbe()
    val ponger = TestProbe()

    def router = spawn router {
      case Ping => pinger.ref
      case Pong => ponger.ref
    }

    router ! Ping
    pinger.expectMsg(Ping)

    router ! Pong
    ponger.expectMsg(Pong)
  }


  it should "spawn one-time message router bound to given actor" in {
    val pinger = TestProbe()
    val router = spawn.router.to(pinger.ref)
    router ! Ping
    pinger.expectMsg(Ping)
  }
}


// vim: set ts=2 sw=2 et:
