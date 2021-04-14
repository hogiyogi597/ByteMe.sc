package com.github.hogiyogi597.yarn

import com.github.hogiyogi597.yarn.YarnResult._
import org.http4s.Uri

case class YarnResult(url: Uri, gif: Uri, title: Title, transcript: Transcript, duration: Duration)

object YarnResult {
  type Title      = String
  type Transcript = String
  type Duration   = Double
}
