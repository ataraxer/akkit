package com.ataraxer.akkit.aggregator

import com.ataraxer.akkit.UnitSpec

import org.scalatest._

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{ActorSystem, ActorRef}

import scala.concurrent.duration._
import scala.util.Random


object CounterSpec {
  case object Foo
  case object Bar
  case object Baz

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


class CounterSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
    with CounterSpawner
{
  import Counter._
  import CounterSpec._

  def this() = this(ActorSystem("counter-spec"))

  val context = system


  "A Counter" should "count expected messages" in {
    val counter = count(Foo, Bar)
    generateMessages foreach { counter ! _ }

    counter ! Flush

    expectMsgPF(1.second) {
      case Count(count) =>
        count should be (fooCount + barCount)
    }
  }


  it should "count expected messages defined by partial function" in {
    val counter = count matching {
      case Foo => true
      case Bar => true
      case List(Foo, Bar | Foo) => true
    }

    generateMessages foreach { counter ! _ }
    counter ! List(Foo, Bar)
    counter ! List(Bar, Foo)
    counter ! List(Bar, Bar)
    counter ! List(Foo, Foo)

    counter ! Flush

    expectMsgPF(1.second) {
      case Count(count) =>
        count should be (fooCount + barCount + 2)
    }
  }


  it should "alert client on expected count of expected messages" in {
    val counter = count.expect(fooCount + barCount)(Foo, Bar)
    generateMessages foreach { counter ! _ }

    expectMsg(Done)
  }


  it should "alert client on expected count of messages defined by PF" in {
    val counter = count.expect(fooCount + barCount + 2).matching {
      case Foo => true
      case Bar => true
      case List(Foo, Bar | Foo) => true
    }

    generateMessages foreach { counter ! _ }
    counter ! List(Foo, Bar)
    counter ! List(Bar, Foo)
    counter ! List(Bar, Bar)
    counter ! List(Foo, Foo)

    expectMsg(Done)
  }


  it should "count distinct number of expected messages" in {
    val counter = count.distinct(Foo, Bar)
    generateMessages foreach { counter ! _ }

    counter ! Flush

    expectMsgPF(1.seconds) {
      case DistinctCount(result) =>
        result should be { Map(Foo -> fooCount, Bar -> barCount) }
    }
  }


  it should "count distinct number of expected messages defined by PF" in {
    val counter = count.distinct.matching {
      case Foo => true
      case Bar => true
      case List(Foo, Bar | Foo) => true
    }

    generateMessages foreach { counter ! _ }
    counter ! List(Foo, Bar)
    counter ! List(Bar, Foo)
    counter ! List(Bar, Bar)
    counter ! List(Foo, Foo)

    counter ! Flush

    expectMsgPF(1.seconds) {
      case DistinctCount(result) =>
        result should be { Map(
          Foo -> fooCount,
          Bar -> barCount,
          List(Foo, Foo) -> 1,
          List(Foo, Bar) -> 1
        )}
    }
  }


  it should "return collected count on Peak message" in {
    val counter = count(Foo, Bar)
    generateMessages foreach { counter ! _ }

    counter ! Peak

    val originalResult =
      expectMsgPF(1.second) {
        case Count(result) => result
      }

    counter ! Peak

    expectMsg(Count(originalResult))
  }


  it should "return collected count and reset it's state " +
            "on Peak message" in
  {
    val counter = count(Foo, Bar)
    generateMessages foreach { counter ! _ }

    counter ! Flush

    expectMsgPF(1.second) {
      case _: Count =>
    }

    counter ! Peak

    expectMsg(Count(0))
  }
}


// vim: set ts=2 sw=2 et:
