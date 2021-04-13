package com.github.hogiyogi597

import cats.implicits._
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
}
