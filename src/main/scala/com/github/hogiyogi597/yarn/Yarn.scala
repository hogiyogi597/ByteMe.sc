package com.github.hogiyogi597.yarn

import cats.effect.Sync
import cats.implicits._
import com.github.hogiyogi597.yarn.JsoupBrowserInterpreter._
import org.http4s.Uri

trait Yarn[F[_]] {
  def getPopular: F[Option[YarnResult]]
  def searchTerm(term: String): F[Option[YarnResult]]
  def multiSearchTerm(term: String): F[List[YarnResult]]
}

object Yarn {
  def apply[F[_]](implicit Yarn: Yarn[F]): Yarn[F] = Yarn
}

class JsoupBrowserInterpreter[F[_]: Sync: YarnParser] extends Yarn[F] {
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

object JsoupBrowserInterpreter {
  protected val popularRequestUrl: Uri                  = baseYarnUrl.withPath("yarn-popular")
  protected def searchTermRequestUrl(term: String): Uri = baseYarnUrl.withPath("yarn-find").withQueryParam("text", term)
}
