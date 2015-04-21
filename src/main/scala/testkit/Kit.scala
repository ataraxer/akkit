package akkit
package testkit

import akka.actor._
import akka.testkit._


class Kit(implicit system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender


// vim: set ts=2 sw=2 et sts=2:
