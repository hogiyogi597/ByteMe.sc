package com.github.hogiyogi597.models

sealed trait ParsedCommand {
  val command: List[String]
  val description: String
}

object ParsedCommand {

  case class HelpCommand() extends ParsedCommand {
    override val command: List[String] = List(" --help", "?").map(commandPrefix + _)
    override val description: String   = "Prints this message to the channel"
  }

  case class RandomSearchCommand() extends ParsedCommand {
    override val command: List[String] = List(commandPrefix)
    override val description: String   = "Provides a random sound byte"
  }

  case class SingleSearchCommand(searchPhrase: String) extends ParsedCommand {
    override val command: List[String] = List(" [your search phrase]").map(commandPrefix + _)
    override val description: String   = "Provides a sound byte that matches your search phrase"
  }

  case class MultiSearchCommand(searchPhrase: String) extends ParsedCommand {
    override val command: List[String] = List("* [your search phrase]").map(commandPrefix + _)
    override val description: String   = "Provides 10 sound bytes that match your search phrase"
  }

  case class CompleteMultiSearchCommand(selectedIndex: Int) extends ParsedCommand {
    override val command: List[String] = List("[the desired sound byte's number]")
    override val description: String   = "Provides the selected sound byte"
  }

  case class CancelMultiSearchCommand() extends ParsedCommand {
    override val command: List[String] = List("cancel")
    override val description: String   = "Cancels any pending searches for the user"
  }

  // TODO: Figure out a better way of doing this... I wanted to leverage the same ParsedCommand type to include commadnds and descriptions but
  // having them be case classes makes it awkward.
  def findValues = List(
    HelpCommand(),
    RandomSearchCommand(),
    SingleSearchCommand(""),
    MultiSearchCommand(""),
    CompleteMultiSearchCommand(0),
    CancelMultiSearchCommand()
  )
}
