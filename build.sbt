name := "scala-pp"

version := "0.0.3-SNAPSHOT"

organization := "com.todesking"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

scalacOptions ++= Seq("-deprecation", "-feature")

publishTo := Some(Resolver.file("com.todesking",file("./repo/"))(Patterns(true, Resolver.mavenStyleBasePattern)))
