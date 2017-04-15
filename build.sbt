name := "ngst"

version := "0.1.0-SNAPSHOT"

description := "An GraphQL server for stellar"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.1.0",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.slick" %% "slick" % "3.2.0"
)
