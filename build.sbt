name := "akkit"

version := "0.1.1-SNAPSHOT"

organization := "com.ataraxer"

homepage := Some(url("http://github.com/ataraxer/akkit"))

licenses := Seq("MIT License" -> url(
  "http://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-g:vars",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings")

/* ==== DEPENDENCIES ==== */
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.3.3",
  "com.typesafe.akka" %% "akka-slf4j"   % "2.3.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test",
  "org.scalatest" %% "scalatest" % "2.1.0" % "test",
  "log4j" % "log4j" % "1.2.15" excludeAll (
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "javax.jmx")),
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
    exclude("org.slf4j", "slf4j-simple"))

/* ==== PUBLISHING ==== */
publishTo <<= version { (ver: String) =>
  val nexus = "http://nexus.ataraxer.com/nexus/"
  if (ver.trim.endsWith("SNAPSHOT")) {
    Some("Snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("Releases"  at nexus + "content/repositories/releases")
  }
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { case _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:ataraxer/akkit.git</url>
    <connection>scm:git:git@github.com:ataraxer/akkit.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ataraxer</id>
      <name>Anton Karamanov</name>
      <url>github.com/ataraxer</url>
    </developer>
  </developers>)

/* ==== PLUGINS ==== */
// Scoverage support
instrumentSettings

// Site publishing (API)
site.settings

site.includeScaladoc()

ghpages.settings

git.remoteRepo := "git@github.com:ataraxer/akkit.git"

/* ==== OTHER ==== */
testOptions in Test += Tests.Setup { classLoader =>
  classLoader
  .loadClass("org.slf4j.LoggerFactory")
  .getMethod("getLogger", classLoader.loadClass("java.lang.String"))
  .invoke(null, "ROOT")
}

