import BuildVars._

name := "atom-manager-play"

version := "1.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.play"      %% "play"                  % playVersion,
  "com.gu"                 %% "content-atom-model"    % contentAtomVersion,
  "org.typelevel"          %% "cats-core"             % "0.6.0",
  "org.scalatestplus.play" %% "scalatestplus-play"    % "1.5.0"   % "test",
  "org.mockito"            %  "mockito-core"          % mockitoVersion % "test"
    //"com.typesafe.play" %% "play-ws" % playVersion
)
