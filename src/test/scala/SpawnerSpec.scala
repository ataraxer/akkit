package com.ataraxer.akkit

import akka.testkit.{TestKit, TestActorRef, TestProbe, ImplicitSender}
import akka.actor.{ActorSystem, Actor, ActorRef}

import scala.concurrent.duration._


object SpawnerSpec {
  case object Ping
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

  val context = system


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
}


// vim: set ts=2 sw=2 et:
