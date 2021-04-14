package com.github.hogiyogi597.yarn

import cats.effect.Sync
import cats.implicits._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, elementList, text}
import org.http4s.Uri

trait YarnParser[F[_]] {
  def parseDocFromUrl(uri: Uri): F[List[YarnResult]]
}

object YarnParser {
  def apply[F[_]](implicit YarnParser: YarnParser[F]): YarnParser[F] = YarnParser
}

// TODO: add config values for each of the css classes used to get each part of YarnResult
class JsoupYarnParserInterpreter[F[_]: Sync] extends YarnParser[F] {
  private val browser: Browser                          = JsoupBrowser()
  private val yarnResultsLimit                          = 10L
  private val thumbnailGuidRegex                        = "[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?".r
  private def createThumbnailGif(thumbnailGuid: String) = Uri.fromString(s"https://y.yarn.co/${thumbnailGuid}_200_10.gif").toOption

  override def parseDocFromUrl(uri: Uri): F[List[YarnResult]] =
    Sync[F]
      .delay(browser)
      .map(_.get(uri.renderString))
      .flatMap(parseDocForYarnResult)

  private def parseDocForYarnResult(doc: Document): F[List[YarnResult]] = {
    fs2.Stream
      .fromIterator((doc >> elementList(".clip")).iterator)
      .map { e =>
        (
          (e >?> attr("href")("a")).map(baseYarnUrl.addPath),
          (e >?> attr("alt")(".match")).flatMap(thumbnailGuidRegex.findFirstIn).flatMap(createThumbnailGif),
          e >?> text(".title"),
          e >?> text(".transcript"),
          (e >?> text(".play-time")).flatMap(_.split(" ").flatMap(_.toDoubleOption).headOption)
        ).mapN(YarnResult.apply)
      }
      .unNone
      .take(yarnResultsLimit)
      .compile
      .toList
  }
}
