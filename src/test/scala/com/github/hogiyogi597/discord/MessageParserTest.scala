package com.github.hogiyogi597.discord;

import cats.syntax.all._
import com.github.hogiyogi597.models.ParsedCommand
import com.github.hogiyogi597.models.ParsedCommand.{CancelMultiSearchCommand, CompleteMultiSearchCommand, HelpCommand, MultiSearchCommand, RandomSearchCommand, SingleSearchCommand}
import weaver._

object MessageParserTest extends SimpleIOSuite {
  val searchPhrase = "search phrase"

  val commandMap: Map[String, Option[ParsedCommand]] = Map(
    "/byteme --help"              -> HelpCommand().some,
    "/byteme?"                    -> HelpCommand().some,
    "/byteme? some random string" -> HelpCommand().some,
    "/byteme"                     -> RandomSearchCommand().some,
    s"/byteme $searchPhrase"      -> SingleSearchCommand(searchPhrase).some,
    s"/byteme* $searchPhrase"     -> MultiSearchCommand(searchPhrase).some,
    "/byteme*"                    -> MultiSearchCommand("").some,
    "1"                           -> CompleteMultiSearchCommand(1).some,
    "cancel"                      -> CancelMultiSearchCommand().some,
    "/bytem"                      -> None,
    "cance"                       -> None,
    "unrecognized command"        -> None
  )

  commandMap.foreach { case (input, expectedCommand) =>
    pureTest(s"Input: '$input'")(expectParsedMessage(input, expectedCommand))
  }

  private def expectParsedMessage(inputMessage: String, expectedOutput: Option[ParsedCommand]): Expectations = {
    expect(MessageParser.parseMessage(inputMessage) == expectedOutput)
  }
}
