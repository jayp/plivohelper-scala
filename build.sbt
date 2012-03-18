name := "Plivo Scala helper Library"

organization := "org.plivo"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.1"

// for xmldiff
resolvers += "patel.org.in repo" at "http://code.patel.org.in/repo-releases/"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-lift-json" % "0.8.5",
  "net.databinder" %% "unfiltered-jetty" % "0.6.1",
  "net.databinder" %% "unfiltered-filter" % "0.6.1",
  "org.slf4j" % "slf4j-api" % "1.6.1",
  "ch.qos.logback" % "logback-classic" % "0.9.26",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "in.org.patel" %% "xmldiff" % "0.4" % "test"
)
