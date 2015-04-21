package akkit
package testkit

import akka.actor._
import akka.testkit._


class ActorSpec(systemName: String)
  extends TestKit(ActorSystem(systemName))


// vim: set ts=2 sw=2 et sts=2:
