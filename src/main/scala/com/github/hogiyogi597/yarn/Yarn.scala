package com.github.hogiyogi597.yarn

import cats.effect.{Effect, Sync}
import cats.free.Free
import cats.implicits._
import cats.{InjectK, ~>}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.Uri
import org.http4s.implicits._

sealed trait Yarn[A]
case class Popular()                 extends Yarn[Option[YarnResult]]
case class Search(term: String)      extends Yarn[Option[YarnResult]]
case class MultiSearch(term: String) extends Yarn[List[YarnResult]]

object Yarn {
  class Ops[F[_]](implicit I: InjectK[Yarn, F]) {
    def getPopular: Free[F, Option[YarnResult]] = Free.liftInject(Popular())
    def searchTerm(term: String): Free[F, Option[YarnResult]] =
      Free.liftInject(Search(term))
    def multiSearchTerm(term: String): Free[F, List[YarnResult]] =
      Free.liftInject(MultiSearch(term))
  }

  object Ops {
    implicit def yarnOps[F[_]](implicit I: InjectK[Yarn, F]): Ops[F] = new Ops[F]
  }
}

object JsoupBrowserInterpreter {
  private val baseUrl                            = uri"https://getyarn.io"
  private val popularRequestUrl                  = baseUrl.withPath("yarn-popular")
  private def searchTermRequestUrl(term: String) = baseUrl.withPath("yarn-find").withQueryParam("text", term)
  private lazy val browser                       = JsoupBrowser()

  def interpreter[F[_]: Effect]: Yarn ~> F = new (Yarn ~> F) {
    override def apply[A](fa: Yarn[A]): F[A] = fa match {
      case Popular() =>
        getYarnRequest(popularRequestUrl)
          .map(_.headOption)
      case Search(term) =>
        getYarnRequest(searchTermRequestUrl(term))
          .map(_.headOption)
      case MultiSearch(term) =>
        getYarnRequest(searchTermRequestUrl(term))
    }
  }

  private def getYarnRequest[F[_]: Effect](url: Uri)(implicit sync: Sync[F]) =
    sync
      .delay(browser)
      .map(_.get(url.renderString))
      .flatMap(parseDocForYarnResult[F]())

  private val thumbnailGuidRegex                        = "[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?".r
  private def createThumbnailGif(thumbnailGuid: String) = Uri.fromString(s"https://y.yarn.co/${thumbnailGuid}_200_10.gif").toOption

  private def parseDocForYarnResult[F[_]: Effect](limit: Int = 10)(doc: Document): F[List[YarnResult]] =
    fs2.Stream
      .fromIterator((doc >> elementList(".clip")).iterator)
      .map { e =>
        (
          (e >?> attr("href")("a")).map(baseUrl.addPath),
          (e >?> attr("alt")(".match")).flatMap(thumbnailGuidRegex.findFirstIn).flatMap(createThumbnailGif),
          e >?> text(".title"),
          e >?> text(".transcript"),
          (e >?> text(".play-time")).flatMap(_.split(" ").flatMap(_.toDoubleOption).headOption)
        ).mapN(YarnResult.apply)
      }
      .unNone
      .take(limit.toLong)
      .compile
      .toList

}
