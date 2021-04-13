package com.github.hogiyogi597.discord

import atto.Atto.{stringCI, takeText, token, _}
import atto.Parser
import cats.free.Free
import cats.{Applicative, InjectK, ~>}
import com.github.hogiyogi597.models._

sealed trait MessageParser[A]
case class ParseMessage(message: String) extends MessageParser[Option[ParsedCommand]]

object MessageParser {
  class Ops[F[_]](implicit I: InjectK[MessageParser, F]) {
    def parseMessage(message: String): Free[F, Option[ParsedCommand]] = Free.liftInject(ParseMessage(message))
  }

  object Ops {
    implicit def messageParserOps[F[_]](implicit I: InjectK[MessageParser, F]): Ops[F] = new Ops[F]()
  }
}

object AttoInterpreter {
  private val command = "/byteme"

  private val botCommandParser    = token(stringCI(command))
  private val simpleCommandParser = botCommandParser ~> token(takeText)

  private val randomSearchParser        = simpleCommandParser.filter(_.isEmpty).map(_ => RandomSearchCommand())
  private val singleSearchParser        = simpleCommandParser.filter(_.nonEmpty).map(SingleSearchCommand)
  private val multiSearchParser         = (botCommandParser ~ char('*') ~> token(takeText)).map(MultiSearchCommand)
  private val completeMultiSearchParser = decimalDigit.map(_.asDigit).map(CompleteMultiSearchCommand)
  private val helpCommandParser         = (botCommandParser ~ token(stringCI("--help"))).map(_ => HelpCommand())
  private val cancelCommandParser       = token(string("cancel")).map(_ => CancelMultiSearchCommand())

  def parseCommands[F[_]]: Parser[ParsedCommand] = cancelCommandParser | helpCommandParser | multiSearchParser | randomSearchParser | singleSearchParser | completeMultiSearchParser

  def interpreter[F[_]: Applicative]: MessageParser ~> F = new (MessageParser ~> F) {
    override def apply[A](fa: MessageParser[A]): F[A] = fa match {
      case ParseMessage(message) =>
        Applicative[F].pure(parseCommands.parseOnly(message).option)
    }
  }
}
