import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.cmhh",
      scalaVersion := "2.12.8",
      version      := "0.1.0"
    )),
    name := "snzscrape",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "3.141.59",
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.141.59",
    libraryDependencies += "com.codeborne" % "phantomjsdriver" % "1.4.4",
    mainClass in assembly := Some("org.cmhh.Main"),
    assemblyJarName in assembly := "snzscrape.jar"
  )
