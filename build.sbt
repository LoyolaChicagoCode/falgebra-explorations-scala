name := "droste-explorations-scala"

version := "0.2"

scalaVersion := "3.1.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Yexplicit-nulls",
  "-language:strictEquality",
  "-language:higherKinds",
  "-Ykind-projector:underscores"
)

libraryDependencies ++= Seq(
  "io.higherkindness" %% "droste-core" % "0.9.0",
  "io.chrisdavenport" %% "cats-scalacheck" % "0.3.1" % Test,
  "org.typelevel" %% "cats-laws" % "2.8.0" % Test
)

scalacOptions ++= Seq("-rewrite", "-new-syntax")
