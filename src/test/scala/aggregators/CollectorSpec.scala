package com.ataraxer.akkit.aggregator

import com.ataraxer.akkit.UnitSpec

import org.scalatest._

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{ActorSystem, ActorRef}

import scala.concurrent.duration._
import scala.util.Random


object CollectorSpec {
  case object Foo
  case object Bar
  case object Baz
  case class Content(value: String)

  val fooCount = 5
  val barCount = 10
  val bazCount = 15

  def generateMessages =
    Random.shuffle(
      List.fill(fooCount)(Foo) ++
      List.fill(barCount)(Bar) ++
      List.fill(bazCount)(Baz)
    )
}


class CollectorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
    with CollectorSpawner
{
  import Collector._
  import CollectorSpec._

  val context = _system

  def this() = this(ActorSystem("collector-spec"))

  "A Collector" should "collect expected messages" in {
    val collector = collect(Foo, Bar)
    generateMessages foreach { collector ! _ }

    collector ! Flush

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar)
        result.count(_ == Foo) should be (fooCount)
        result.count(_ == Bar) should be (barCount)
      }
    }
  }


  it should "collect values extracted with partial function" in {

    val collector = collect matching {
      case Foo => Bar
      case Bar => Foo
      case Content(value) => value
    }

    generateMessages foreach { collector ! _ }
    collector ! Content("foobar")

    collector ! Flush

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar, "foobar")
        result.count(_ == Foo) should be (barCount)
        result.count(_ == Bar) should be (fooCount)
      }
    }
  }


  it should "collect expected messages untill required count is reached" in {
    val collector = collect.count(fooCount + barCount)(Foo, Bar)
    generateMessages foreach { collector ! _ }

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar)
        result.count(_ == Foo) should be (fooCount)
        result.count(_ == Bar) should be (barCount)
      }
    }
  }


  it should "collect extracted by PF values, " +
            "untill required count is reached" in
  {
    val collector = collect.count(fooCount + barCount + 1) matching {
      case Foo => Bar
      case Bar => Foo
      case Content(value) => value
    }

    generateMessages foreach { collector ! _ }
    collector ! Content("foobar")

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar, "foobar")
        result.count(_ == Foo) should be (barCount)
        result.count(_ == Bar) should be (fooCount)
      }
    }
  }


  it should "return collected items on Peak message" in {
    val collector = collect(Foo, Bar)
    generateMessages foreach { collector ! _ }

    collector ! Peak

    val originalResult =
      expectMsgPF(1.second) {
        case Collection(result) => result
      }

    collector ! Peak

    expectMsg(Collection(originalResult))
  }


  it should "return collected items and reset it's state on Flush message" in {
    val collector = collect(Foo, Bar)
    generateMessages foreach { collector ! _ }

    collector ! Flush

    expectMsgPF(1.second) {
      case _: Collection =>
    }

    collector ! Peak

    expectMsg(Collection(Nil))
  }


  it should "immediately return on non-positive expected size" in {
    collect.count(0)(Foo)
    expectMsg(Collection(Nil))

    collect.count(-1)(Foo)
    expectMsg(Collection(Nil))
  }
}


// vim: set ts=2 sw=2 et:
