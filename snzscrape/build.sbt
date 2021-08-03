import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.cmhh",
      scalaVersion := "2.13.6",
      version      := "0.3.0"
    )),
    name := "snzscrape",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "3.141.59",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.141.59",

    scalacOptions += "-deprecation",

    assembly / mainClass := Some("org.cmhh.Main"),
    assembly / assemblyJarName := "snzscrape.jar",
    
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
  )
