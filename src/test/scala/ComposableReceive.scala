package com.ataraxer.akkit

import com.ataraxer.akkit.testkit._
import akka.actor._


class ComposableReceiveSpec
  extends ActorSpec("composable-receive-spec")
  with UnitSpec
{
  "ComposableReceive" should "compose actor behaviour" in new Kit {
    trait Foo extends ComposableActor {
      stackReceive { case "foo" => sender ! "bar" }
    }

    trait Bar extends ComposableActor {
      stackReceive { case 42 => sender ! 9000 }
    }

    class FooBar extends Foo with Bar

    val foobar = system actorOf Props { new FooBar }
    foobar ! "foo"
    expectMsg("bar")
    foobar ! 42
    expectMsg(9000)
  }
}


// vim: set ts=2 sw=2 et:
