import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "ru.otus"
ThisBuild / organizationName := "otus"

lazy val root = (project in file("."))
  .settings(
    name := "scala-dev-mooc-2024-04",
    libraryDependencies += munit % Test
  )
