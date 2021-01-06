import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.cmhh",
      scalaVersion := "2.13.4",
      version      := "0.3.0"
    )),
    name := "snzscrape",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "3.141.59",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.141.59",
    scalacOptions += "-deprecation",
    mainClass in assembly := Some("org.cmhh.Main"),
    assemblyJarName in assembly := "snzscrape.jar"
  )
