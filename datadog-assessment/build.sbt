name := "datadog-assessment"

version := "0.1"

scalaVersion := "2.13.1"


mainClass in (Compile, packageBin) := Some("com.datadoghq.assessment.MainApp")
mainClass in (Compile, run) := Some("com.datadoghq.assessment.MainApp")
mainClass in assembly := Some("com.datadoghq.assessment.MainApp")
assemblyJarName in assembly := "app.jar"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"
libraryDependencies += "io.monix" %% "monix-eval" % "3.1.0"
libraryDependencies += "commons-validator" % "commons-validator" % "1.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-M1" % Test