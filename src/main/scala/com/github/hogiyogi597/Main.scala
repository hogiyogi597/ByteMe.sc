package com.github.hogiyogi597

import cats.effect._
import cats.syntax.all._
import com.github.hogiyogi597.discord.{AttoInterpreter, DiscordClientInterpreter, DiscordEventHandler, DiscordInteraction, MessageParser}
import com.github.hogiyogi597.persistence.{KVS, LocalUserSearchStoreKVS, UserInteractionStore, UserInteractionStoreInterpreter}
import com.github.hogiyogi597.yarn.{JsoupBrowserInterpreter, JsoupYarnParserInterpreter, Yarn, YarnParser}
import dissonance.Discord
import dissonance.data.events.MessageCreate
import dissonance.data._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val discordToken = args.head

    (Discord.make(discordToken), Resource.eval(LocalUserSearchStoreKVS.make[IO])).tupled
      .use { case (discord, kvs) =>
        implicit val ioKvs: KVS[IO]                                 = kvs
        implicit val yarnParser: YarnParser[IO]                     = new JsoupYarnParserInterpreter[IO]
        implicit val yarn: Yarn[IO]                                 = new JsoupBrowserInterpreter[IO]
        implicit val messageParser: MessageParser[IO]               = new AttoInterpreter[IO]
        implicit val discordInteraction: DiscordInteraction[IO]     = new DiscordClientInterpreter(discord.client)
        implicit val userInteractionStore: UserInteractionStore[IO] = new UserInteractionStoreInterpreter[IO]
        val eventHandler                                            = new DiscordEventHandler[IO]()

        discord
          .subscribe(Shard.singleton, Intent.GuildMessages)
          .evalMap {
            case MessageCreate(BasicMessage(_, content, user, channelId)) if !isBotUser(user) =>
              eventHandler.parseAndHandleCommand(channelId, user, content)
            case _ =>
              IO.unit
          }
          .compile
          .drain
      }
      .as(ExitCode.Success)
  }

  private def isBotUser(user: User) = user.bot.getOrElse(false)
}
