package akkit
package testkit

import akka.actor._
import akka.testkit._

import org.scalatest._

import scala.concurrent.duration._


class KitSpec extends UnitSpec {
  implicit val system = ActorSystem("kit-spec")


  "Kit" should "provide local implicit sender for a test" in new Kit {
    self ! "test"
    expectMsg("test")
  }


  it should "[1/2]: prevent messages from leaking between test cases" in new Kit {
    self ! "leaked"
  }


  it should "[2/2]: prevent messages from leaking between test cases" in new Kit {
    // make sure messages do not leak
    expectNoMsg(3.seconds)
  }


  new TestKit(system) with ImplicitSender {
    "Akka TestKit with ImplicitSender" should "be created" in {}

    it should "process all messages via a single mailbox" in {
      self ! "leaked"
    }

    it should "possibly leak messages between test cases" in {
      expectMsg("leaked")
    }
  }
}


// vim: set ts=2 sw=2 et sts=2:
