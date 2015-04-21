# Akkit [![Build Status](https://travis-ci.org/ataraxer/akkit.svg?branch=master)](https://travis-ci.org/ataraxer/akkit)

Akkit is a collection of usefull general-purpose Akka actors and modules.


## Installation

Akkit is published to Sonatype OSSRH, so all you have to do is add the following
dependencies to your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "com.ataraxer" %% "zooowner-core" % "0.1.0",
  "com.ataraxer" %% "zooowner-macro" % "0.1.0")
```


## @safeActor Macro Annotation

`@safeActor` macro annotation adds following entities to actor's companion object:

- `props` method -- which will accept the same arguments as annotated actor and return an instance of `SafeProps[T]`, where `T` is a type of annotated actor;
- `apply` method -- which will accept the same arguments as annotated actor and implicit `ActorRefFactory` and create a new actor of that type, returning a `SafeRef[T]`, where `T` is a type of annotated actor;
- `Ref` type alias -- equivalent to `SafeRef[T]`, where `T` is a type of annotated actor;
- `<actor class name>Ref` type alias -- as an alias to `Ref`.

This allows you to dramatically decrease boilerplate while defining actors, while making sure that you do not enclose over any unwanted state and passing references to correct actors.

### Demo

```scala
import akkit._
import akka.actor._


@safeActor
class Printer(prefix: String) extends Actor {
  def receive = { case msg => println(prefix -> msg) }
}


@safeActor
class Pinger(printer: Printer.Ref) extends Actor {
  def receive = { case "ping" => printer ! "pong" }
}


object Demo extends App {
  implicit val system = ActorSystem("demo-system")

  val printer = Printer(">>>")
  val pinger = Pinger(printer)

  pinger ! "ping" // results in "ping" printed to stdout
}
```

