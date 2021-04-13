package com.github.hogiyogi597.models

sealed trait ParsedCommand
case class HelpCommand()                                  extends ParsedCommand
case class RandomSearchCommand()                          extends ParsedCommand
case class SingleSearchCommand(searchPhrase: String)      extends ParsedCommand
case class MultiSearchCommand(searchPhrase: String)       extends ParsedCommand
case class CompleteMultiSearchCommand(selectedIndex: Int) extends ParsedCommand
case class CancelMultiSearchCommand()                     extends ParsedCommand
