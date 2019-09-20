val Http4sVersion = "0.18.9"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.5.2"
val CirceVersion = "0.9.3"

lazy val root = (project in file("."))
  .settings(
    organization := "com.perhac.devpro",
    name := "booklog",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-Ypartial-unification", "-feature"),
    scalafmtOnCompile in ThisBuild := true,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion
    ) ++ Circe ++ Doobie
  )

val Circe = Seq(
  "io.circe" %% "circe-core"    % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser"  % CirceVersion,
  "io.circe" %% "circe-java8"   % CirceVersion
)

val Doobie = Seq(
  "org.tpolecat" %% "doobie-core"     % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion
)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)
