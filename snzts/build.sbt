name := """snzts"""
organization := "org.cmhh"

version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

// libraryDependencies += ehcache
libraryDependencies += cacheApi
libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-iteratees" % "2.6.1"
libraryDependencies += "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.17"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.cmhh.controllers._"

// Adds additional packages into conf/routes
play.sbt.routes.RoutesKeys.routesImport += "binders.Binders._"
