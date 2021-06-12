import sbt.Keys.libraryDependencies

scalacOptions += "-Ymacro-annotations"

lazy val root = (project in file("."))
  .settings(
    name := "scala-dev-mooc-2021-03",
    version := "0.1",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Dependencies.zio
  )
testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

scalacOptions += "-Ymacro-annotations"