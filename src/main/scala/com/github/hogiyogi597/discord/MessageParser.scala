package com.github.hogiyogi597.discord

import atto.Atto.{stringCI, takeText, token, _}
import atto.Parser
import cats.Applicative
import com.github.hogiyogi597.models.ParsedCommand
import com.github.hogiyogi597.models.ParsedCommand._
import com.github.hogiyogi597.models._

trait MessageParser[F[_]] {
  def parseMessage(message: String): F[Option[ParsedCommand]]
}

object MessageParser {
  def apply[F[_]](implicit MessageParser: MessageParser[F]): MessageParser[F] = MessageParser
}

class AttoInterpreter[F[_]: Applicative] extends MessageParser[F] {
  private val botCommandParser    = token(stringCI(commandPrefix))
  private val simpleCommandParser = botCommandParser ~> token(takeText)

  private val randomSearchParser        = simpleCommandParser.filter(_.isEmpty).map(_ => RandomSearchCommand())
  private val singleSearchParser        = simpleCommandParser.filter(_.nonEmpty).map(SingleSearchCommand)
  private val multiSearchParser         = (botCommandParser ~ char('*') ~> token(takeText)).map(MultiSearchCommand)
  private val completeMultiSearchParser = decimalDigit.map(_.asDigit).map(CompleteMultiSearchCommand)
  private val helpCommandParser         = (botCommandParser ~ token(stringCI("--help")) || botCommandParser ~ char('?')).map(_ => HelpCommand())
  private val cancelCommandParser       = token(string("cancel")).map(_ => CancelMultiSearchCommand())

  private val parseCommands: Parser[ParsedCommand] =
    cancelCommandParser | helpCommandParser | multiSearchParser | randomSearchParser | singleSearchParser | completeMultiSearchParser

  override def parseMessage(message: String): F[Option[ParsedCommand]] = Applicative[F].pure(parseCommands.parseOnly(message).option)
}
