val CatsVersion       = "2.9.0"
val WeaverCatsVersion = "0.8.3"

ThisBuild / organization      := "com.melvinlow"
ThisBuild / scalaVersion      := "3.3.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = (project in file("."))
  .settings(
    name    := "formify",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"   % CatsVersion,
      "com.disneystreaming" %% "weaver-cats" % WeaverCatsVersion % Test
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Wunused:all",
      "-Werror",
      "-Wvalue-discard",
      "-no-indent",
      "-explain"
    ),
    testFrameworks ++= List(
      new TestFramework("weaver.framework.CatsEffect")
    )
  )
