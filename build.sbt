name := "droste-explorations-scala"

version := "0.1"

scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:higherKinds"
)

libraryDependencies ++= Seq(
  "io.higherkindness" %% "droste-core"              % "0.8.0",
  "org.scalatest"     %% "scalatest"                % "3.2.2",
  "org.scalacheck"    %% "scalacheck"               % "1.14.3"
)
