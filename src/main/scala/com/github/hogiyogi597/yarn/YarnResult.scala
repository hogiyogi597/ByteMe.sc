package com.github.hogiyogi597.yarn

import org.http4s.Uri
import com.github.hogiyogi597.yarn.YarnResult._

case class YarnResult(url: Uri, gif: Uri, title: Title, transcript: Transcript, duration: Duration)

object YarnResult {
  type Title      = String
  type Transcript = String
  type Duration   = Double
}
