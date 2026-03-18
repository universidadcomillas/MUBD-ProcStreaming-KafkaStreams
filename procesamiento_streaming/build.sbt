import Dependencies._

ThisBuild / scalaVersion     := "2.12.18"
ThisBuild / version          := "1.0.0"
ThisBuild / organization     := "edu.comillas.icai"
ThisBuild / organizationName := "edu.comillas.icai"

val sparkVersion = "3.5.7"

lazy val root = (project in file("."))
  .settings(
    name := "procesamiento_streaming",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-streams" % "3.8.0",
      "org.apache.kafka" %% "kafka-streams-scala" % "3.8.0",  // API Scala
      "org.apache.kafka" % "kafka-clients" % "3.8.0",
      "org.slf4j" % "slf4j-simple" % "2.0.13", // logging simple
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "io.circe" %% "circe-core" % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
      munit % Test
    ),
    javaOptions ++= Seq(
      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
