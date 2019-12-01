name := "citrine-assessment"

version := "0.1"

scalaVersion := "2.12.8"

mainClass in Compile := Some("io.citrine.assessment.Application")

val akkaHttp = "10.0.11"
val akka = "2.4.19"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

libraryDependencies ++= Seq(
  // akka dependencies
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,
  "com.typesafe.akka" %% "akka-slf4j" % akka,
  "org.scalatest" %% "scalatest" % "3.0.7" % Test)