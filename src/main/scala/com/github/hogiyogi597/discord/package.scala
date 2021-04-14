package com.github.hogiyogi597

import cats.implicits._
import com.github.hogiyogi597.models.ParsedCommand
import dissonance.data.{Color, Embed, Footer, Image}
import com.github.hogiyogi597.yarn.YarnResult

package object discord {
  def createEmbeddedMessage(yarnResult: YarnResult, index: Int): Embed = {
    import yarnResult._
    Embed.make
      .withTitle(s"$index - $title")
      .withDescription(transcript)
      .withThumbnail(Image(gif.some, None, None, None))
      .withFooter(Footer(duration.toString, None, None))
      .withColor(Color(0x00, 0xff, 0x00))
  }

  val helpMessage: String = ("The following commands are available:" +: ParsedCommand.findValues.map { parsedCommand =>
    s"\t- ${parsedCommand.command.mkString("'", "', '", "'")}\n\t\t\t ${parsedCommand.description}"
  }).mkString("\n")
}
