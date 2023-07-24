val CatsVersion = "2.9.0"
val CatsEffectVersion = "3.5.1"
val CatsEffectTestKitVersion = "3.5.1"
val Fs2Version = "3.7.0"
val Log4CatsVersion = "2.6.0"
val MunitVersion = "0.7.29"
val MunitCatsEffectVersion = "1.0.7"
val WeaverCatsVersion = "0.8.3"
val LogbackVersion = "1.4.8"

ThisBuild / organization := "com.melvinlow"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val root = (project in file("."))
  .settings(
    name := "formify",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"                 % CatsVersion,
      "org.typelevel"              %% "cats-effect"               % CatsEffectVersion,
      "co.fs2"                     %% "fs2-core"                  % Fs2Version,
      "org.typelevel"              %% "log4cats-slf4j"            % Log4CatsVersion,
      "ch.qos.logback"              % "logback-classic"           % LogbackVersion                 % Runtime,
      "org.scalameta"              %% "munit"                     % MunitVersion                   % Test,
      "com.disneystreaming"        %% "weaver-cats"               % WeaverCatsVersion              % Test,
      "org.typelevel"              %% "munit-cats-effect-3"       % MunitCatsEffectVersion         % Test,
      "org.typelevel"              %% "cats-effect-testkit"       % CatsEffectTestKitVersion       % Test
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
      new TestFramework("munit.Framework"),
      new TestFramework("weaver.framework.CatsEffect")
    ),
    assembly / assemblyJarName := "formify.jar",
    assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF")  => MergeStrategy.discard
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x                                    => assemblyMergeStrategy.value(x)
    },
  )