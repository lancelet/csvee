lazy val csvee =
  project
    .in(file("."))
    .settings(
      name := "csvee",
      version := "0.1",
      organization := "com.github.lancelet",
      scalaVersion := "2.11.8",
      libraryDependencies ++= Seq(
        "org.typelevel"  %% "cats"              %  "0.6.1",
        "org.specs2"     %% "specs2-core"       %    "3.5" % "test",
        "org.specs2"     %% "specs2-scalacheck" %    "3.5" % "test",
        "org.scalacheck" %% "scalacheck"        % "1.11.4" % "test"
      )
    )
