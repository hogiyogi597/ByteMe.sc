package com.github.hogiyogi597.yarn

import cats.Functor
import cats.implicits._
import com.github.hogiyogi597.yarn.JsoupYarnBrowser._
import org.http4s.Uri

trait Yarn[F[_]] {
  def getPopular: F[Option[YarnResult]]
  def searchTerm(term: String): F[Option[YarnResult]]
  def multiSearchTerm(term: String): F[List[YarnResult]]
}

object Yarn {
  def apply[F[_]](implicit Yarn: Yarn[F]): Yarn[F] = Yarn
}

class JsoupYarnBrowser[F[_]: Functor: YarnParser] extends Yarn[F] {
  def getPopular: F[Option[YarnResult]] =
    YarnParser[F]
      .parseDocFromUrl(popularRequestUrl)
      .map(_.headOption)

  def searchTerm(term: String): F[Option[YarnResult]] =
    YarnParser[F]
      .parseDocFromUrl(searchTermRequestUrl(term))
      .map(_.headOption)

  def multiSearchTerm(term: String): F[List[YarnResult]] =
    YarnParser[F].parseDocFromUrl(searchTermRequestUrl(term))
}

object JsoupYarnBrowser {
  val yarnPopularPath                                   = "yarn-popular"
  val yarnFindPath                                      = "yarn-find"
  protected val popularRequestUrl: Uri                  = baseYarnUrl.withPath(yarnPopularPath)
  protected def searchTermRequestUrl(term: String): Uri = baseYarnUrl.withPath(yarnFindPath).withQueryParam("text", term)
}
