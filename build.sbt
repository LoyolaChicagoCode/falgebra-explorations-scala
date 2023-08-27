name := "droste-explorations-scala"

version := "0.2"

scalaVersion := "3.1.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Yexplicit-nulls",
  "-Ysafe-init",
  "-language:strictEquality",
  "-language:higherKinds",
  "-Ykind-projector:underscores"
)

libraryDependencies ++= Seq(
  "io.higherkindness" %% "droste-core" % "0.9.0",
  "io.chrisdavenport" %% "cats-scalacheck" % "0.3.2" % Test,
  "org.typelevel" %% "cats-laws" % "2.10.0" % Test
)
