import sbt._
import sbt.Keys._

import com.typesafe.sbt.{SbtSite, SbtGhPages}
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtGit.GitKeys.gitRemoteRepo


object AkkitBuild extends Build {

  val akkaVersion = "2.3.3"
  val scalatestVersion = "2.1.0"

  lazy val commonSettings = Seq(
    scalacOptions ++= Seq(
      "-g:vars",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings"
    ),

    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion,
      // ScalaTest
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      // Logging
      "log4j" % "log4j" % "1.2.15" excludeAll (
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "javax.jms")
      ),
      "org.slf4j" % "slf4j-log4j12" % "1.7.5"
        exclude("org.slf4j", "slf4j-simple")
    ),

    gitRemoteRepo := "git@github.com:ataraxer/akkit.git",

    parallelExecution := true
  )


  lazy val buildSettings =
    Defaults.defaultSettings ++
    SbtSite.site.settings ++
    SbtSite.site.includeScaladoc() ++
    SbtGhPages.ghpages.settings ++
    Seq(
      name         := "akkit",
      version      := "0.1.0",
      scalaVersion := "2.10.3"
    )

  lazy val akkit = Project(
    id = "akkit",
    base = file("."),
    settings = buildSettings
  ).settings(
    commonSettings: _*
  )

}


// vim: set ts=2 sw=2 et:
