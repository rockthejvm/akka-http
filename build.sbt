name := "akka-http"

version := "0.1"

scalaVersion := "2.13.14"

val akkaVersion = "2.6.20"
val akkaHttpVersion = "10.2.10"
val scalaTestVersion = "3.2.7"

libraryDependencies ++= Seq(
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  
  // JWT
  "com.pauldijou" %% "jwt-spray-json" % "5.0.0"
)