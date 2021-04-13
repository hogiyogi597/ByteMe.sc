package com.github.hogiyogi597.discord

import cats.data.EitherK
import cats.effect.IO
import cats.free.Free
import cats.implicits._
import cats.~>
import dissonance.DiscordClient
import dissonance.data.events.MessageCreate
import dissonance.data.{BasicMessage, Event, Snowflake, User}
import com.github.hogiyogi597.models._
import com.github.hogiyogi597.yarn.{JsoupBrowserInterpreter, Yarn}
import com.github.hogiyogi597.Programs._
import com.github.hogiyogi597.persistence.{LocalUserInteractionStateStoreInterpreter, UserInteractionStore}

class DiscordEventHandler(discordClient: DiscordClient) {
  type Eff1[A] = EitherK[MessageParser, UserInteractionStore, A]
  type Eff0[A] = EitherK[DiscordInteraction, Eff1, A]
  type Eff[A]  = EitherK[Yarn, Eff0, A]
  val temp = LocalUserInteractionStateStoreInterpreter.make[IO].unsafeRunSync() // TODO: fix this
  private val interpreter: Eff ~> IO =
    JsoupBrowserInterpreter.interpreter[IO] or (DiscordClientInterpreter.interpreter(discordClient) or (AttoInterpreter.interpreter[IO] or temp.interpreter))

  def handleEvent: Event => IO[Unit] = {
    case MessageCreate(BasicMessage(_, content, user, channelId)) if !isBotUser(user) =>
      parseAndHandleCommand[Eff](channelId, user, content).foldMap(interpreter)
    case _ =>
      IO.unit
  }

  def parseAndHandleCommand[S[_]: Yarn.Ops: DiscordInteraction.Ops: UserInteractionStore.Ops](
      channelId: Snowflake,
      user: User,
      rawInputMessage: String
  )(implicit parser: MessageParser.Ops[S]): Free[S, Unit] = {
    for {
      parsedCommand <- parser.parseMessage(rawInputMessage)
      _ <- parsedCommand.traverse {
             case HelpCommand()                             => ???
             case RandomSearchCommand()                     => getRandom(channelId)
             case SingleSearchCommand(searchPhrase)         => getFromSearchString(channelId, searchPhrase)
             case MultiSearchCommand(searchPhrase)          => getMultiSearchResults(channelId, user, searchPhrase)
             case CompleteMultiSearchCommand(selectedIndex) => completeMultiSearchCommand(channelId, user.id.value, selectedIndex)
             case CancelMultiSearchCommand()                => cancelMultiSearchCommand(user.id.value)
           }
    } yield ()
  }

  private def isBotUser(user: User) = user.bot.getOrElse(false)
}
