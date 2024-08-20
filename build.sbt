name := "falgebra-explorations-scala"

version := "0.1"

scalaVersion := "3.3.3"

scalacOptions += "@.scalacOptions.txt"

scalacOptions += "-Ykind-projector:underscores"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
)
