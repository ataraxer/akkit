import SonatypeKeys._


val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  organization := "com.ataraxer",
  homepage := Some(url("http://github.com/ataraxer/akkit")),

  scalaVersion := "2.11.6",
  crossScalaVersions := Seq("2.10.5", "2.11.6"),

  licenses := Seq("MIT License" -> url(
    "http://www.opensource.org/licenses/mit-license.php")),

  scalacOptions ++= Seq(
    "-g:vars",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Xfatal-warnings"))


val akkaVersion = "2.3.9"

val dependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"))


val macroDependencies = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5"
    cross CrossVersion.full))


val publishingSettings = sonatypeSettings ++ Seq(
  publishArtifact in Test := false,
  profileName := "ataraxer",
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
    </developers>))


val ghPagesSettings = {
  site.settings ++
  ghpages.settings ++
  site.includeScaladoc("") ++ Seq {
    git.remoteRepo := "git@github.com:ataraxer/akkit.git"
  }
}


val projectSettings = {
  commonSettings ++
  instrumentSettings ++
  publishingSettings ++
  dependencies
}


lazy val akkit = (project in file("."))
  .aggregate(akkitCore, akkitMacro)

lazy val akkitCore = (project in file("akkit-core"))
  .settings(name := "akkit-core")
  .settings(projectSettings: _*)
  .settings(ghPagesSettings: _*)

lazy val akkitMacro = (project in file("akkit-macro"))
  .settings(name := "akkit-macro")
  .dependsOn(akkitCore % "test->test")
  .settings(projectSettings: _*)
  .settings(macroDependencies: _*)

