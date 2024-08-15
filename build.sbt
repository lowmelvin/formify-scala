val CatsVersion       = "2.12.0"
val WeaverCatsVersion = "0.8.4"

ThisBuild / organization     := "com.melvinlow"
ThisBuild / organizationName := "Melvin Low"

ThisBuild / scalaVersion       := "3.4.3"
ThisBuild / crossScalaVersions := Seq(scalaVersion.value)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/lowmelvin/formify-scala"))
ThisBuild / developers := List(
  Developer("lowmelvin", "Melvin Low", "me@melvinlow.com", url("https://melvinlow.com"))
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"

usePgpKeyHex("821A82C15670B776F9950C8046E96DBCFD1E8107")

lazy val root = (project in file("."))
  .settings(
    name        := "formify",
    description := "Scala library to convert generic product types to form data",
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

lazy val docs = (project in file("formify-docs"))
  .dependsOn(root)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocIn  := file("docs/README.md"),
    mdocOut := file("README.md")
  )
