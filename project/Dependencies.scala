import sbt._

object Dependencies {

  object Versions {
    val fs2          = "2.3.0"
    val atto         = "0.9.1"
    val cats         = "2.4.2"
    val circe        = "0.13.0"
    val skunk        = "0.0.23"
    val slf4j        = "1.7.30"
    val http4s       = "0.21.7"
    val dissonance   = "0.0.0+363-c9a1619f-SNAPSHOT"
    val catsEffect   = "2.2.0"
    val commonsText  = "1.9"
    val scalaScraper = "2.2.0"
  }

  object Compile {
    val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
    val cats =
      Seq("cats-core", "cats-free").map("org.typelevel" %% _ % Versions.cats)
    val atto = "org.tpolecat" %% "atto-core" % Versions.atto
    val circe = Seq("circe-core", "circe-parser", "circe-generic-extras").map(
      "io.circe" %% _ % Versions.circe
    )
    val skunk        = "org.tpolecat"         %% "skunk-core"    % Versions.skunk
    val slf4j        = "org.slf4j"             % "slf4j-nop"     % Versions.slf4j
    val http4s       = "org.http4s"           %% "http4s-circe"  % Versions.http4s
    val dissonance   = "com.github.billzabob" %% "dissonance"    % Versions.dissonance
    val catsEffect   = "org.typelevel"        %% "cats-effect"   % Versions.catsEffect
    val commonsText  = "org.apache.commons"    % "commons-text"  % Versions.commonsText
    val scalaScraper = "net.ruippeixotog"     %% "scala-scraper" % Versions.scalaScraper
  }

  import Compile._

  lazy val dependencies = Seq(
    fs2,
    atto,
    skunk,
    slf4j,
    http4s,
    dissonance,
    catsEffect,
    commonsText,
    scalaScraper
  ) ++ cats ++ circe
}
