name := """snzts"""
organization := "org.cmhh"
maintainer := "cmhhansen@outlook.com"

version := "0.3.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.18"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"

// Adds additional packages into conf/routes
play.sbt.routes.RoutesKeys.routesImport += "binders.Binders._"