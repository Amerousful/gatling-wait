name := "gatling-wait"

version := "1.0"

scalaVersion := "2.13.15"

val gatlingVersion = "3.9.5"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-http" % gatlingVersion % "provided",
  "io.gatling" % "gatling-core" % gatlingVersion % "provided",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test",
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)