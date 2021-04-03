package yarn

import yarn.YarnResult._

case class YarnResult(url: Url,
                      gif: GifUrl,
                      title: Title,
                      transcript: Transcript,
                      duration: Duration)

object YarnResult {
  type Title = String
  type Transcript = String
  type GifUrl = String
  type Url = String
  type Duration = Double
}
