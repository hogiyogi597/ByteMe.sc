package com.github.hogiyogi597.yarn

import cats.Id
import cats.effect.Sync
import cats.syntax.all._
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
class JsoupYarnParser[F[_]: Sync] extends YarnParser[F] {
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

object TestYarnParser extends YarnParser[Id] {
  val yarnResult1: YarnResult =
    YarnResult(
      Uri.unsafeFromString("https://y.yarn.co/fa5a4308-5072-4d07-9a11-9688566f2082.mp4"),
      Uri.unsafeFromString("https://y.yarn.co/fa5a4308-5072-4d07-9a11-9688566f2082_200_10.gif"),
      "Monty Python and the Holy Grail",
      "- Now stand aside, worthy adversary.- 'Tis but a scratch.",
      3.9
    )
  val yarnResult2: YarnResult =
    YarnResult(
      Uri.unsafeFromString("https://y.yarn.co/1cc45533-e8c0-4849-a25a-3dabaf8015b6.mp4"),
      Uri.unsafeFromString("https://y.yarn.co/1cc45533-e8c0-4849-a25a-3dabaf8015b6_200_10.gif"),
      "Silicon Valley",
      "Call that D2F.",
      1.9
    )

  private val yarnParserResults: Map[Uri, List[YarnResult]] = Map(
    Uri.unsafeFromString("")                    -> List(yarnResult1, yarnResult2),
    Uri.unsafeFromString("search phrase")       -> List(yarnResult2),
    Uri.unsafeFromString("multi search phrase") -> List(yarnResult2, yarnResult1, yarnResult1)
  )

  override def parseDocFromUrl(uri: Uri): Id[List[YarnResult]] = yarnParserResults.getOrElse(uri, List.empty)
}
