import Dependencies._

ThisBuild / scalaVersion     := "2.13.16"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "mytest",
    libraryDependencies += munit % Test
  )

ThisBuild / scalaVersion := "2.12.20"

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
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2"
)


