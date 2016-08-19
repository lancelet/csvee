lazy val csvee =
  project
    .in(file("."))
    .settings(
      name := "csvee",
      version := "0.1",
      organization := "com.github.lancelet",
      scalaVersion := "2.11.8",
      libraryDependencies ++= Seq(
        "org.specs2"     %% "specs2-core"       %  "3.8.4" % "test",
        "org.specs2"     %% "specs2-scalacheck" %  "3.8.4" % "test",
        "org.scalacheck" %% "scalacheck"        % "1.13.2" % "test"
      )
    )
