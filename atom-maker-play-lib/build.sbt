scalaVersion := "2.11.8"

name := "atom-maker-play-lib"

version := "1.0.0-SNAPSHOT"

lazy val playVersion = "2.5.4"

libraryDependencies ++= Seq(
  "com.gu"            %% "content-atom-model" % "1.0.1",
  "com.typesafe.play" %% "play"               % playVersion,
  "org.typelevel"     %% "cats-core"          % "0.6.0" // for interacting with scanamo
)
