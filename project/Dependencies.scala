import sbt._

object Dependencies {

  object Versions {
    val fs2          = "2.3.0"
    val atto         = "0.9.1"
    val cats         = "2.4.2"
    val slf4j        = "1.7.30"
    val http4s       = "0.21.7"
    val dissonance   = "0.0.0+363-c9a1619f-SNAPSHOT"
    val catsEffect   = "2.4.1"
    val scalaScraper = "2.2.0"
    val weaverTest   = "0.7.4"
  }

  object Compile {
    val fs2          = "co.fs2"               %% "fs2-core"      % Versions.fs2
    val cats         = Seq("cats-core", "cats-free").map("org.typelevel" %% _ % Versions.cats)
    val atto         = "org.tpolecat"         %% "atto-core"     % Versions.atto
    val slf4j        = "org.slf4j"             % "slf4j-nop"     % Versions.slf4j
    val http4s       = "org.http4s"           %% "http4s-circe"  % Versions.http4s
    val dissonance   = "com.github.billzabob" %% "dissonance"    % Versions.dissonance
    val catsEffect   = "org.typelevel"        %% "cats-effect"   % Versions.catsEffect
    val scalaScraper = "net.ruippeixotog"     %% "scala-scraper" % Versions.scalaScraper
    val weaverTest   = "com.disneystreaming"  %% "weaver-cats"   % Versions.weaverTest % Test
  }

  import Compile._

  lazy val dependencies = Seq(fs2, atto, slf4j, http4s, dissonance, catsEffect, scalaScraper, weaverTest) ++ cats
}
