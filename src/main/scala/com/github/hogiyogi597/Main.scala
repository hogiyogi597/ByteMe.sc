package com.github.hogiyogi597

import cats.effect._
import cats.syntax.all._
import com.github.hogiyogi597.discord.{DiscordClientInteraction, DiscordEventHandler, DiscordInteraction}
import com.github.hogiyogi597.persistence.{KVS, LocalUserSearchStoreKVS, UserInteractionStore, LiveUserInteractionStore}
import com.github.hogiyogi597.yarn.{JsoupYarnBrowser, JsoupYarnParser, Yarn, YarnParser}
import dissonance.Discord
import dissonance.data._
import dissonance.data.events.MessageCreate

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val discordToken = args.head

    (Discord.make(discordToken), Resource.eval(LocalUserSearchStoreKVS.make[IO])).tupled
      .use { case (discord, kvs) =>
        implicit val ioKvs: KVS[IO]                                 = kvs
        implicit val yarnParser: YarnParser[IO]                     = new JsoupYarnParser[IO]
        implicit val yarn: Yarn[IO]                                 = new JsoupYarnBrowser[IO]
        implicit val discordInteraction: DiscordInteraction[IO]     = new DiscordClientInteraction(discord.client)
        implicit val userInteractionStore: UserInteractionStore[IO] = new LiveUserInteractionStore[IO]
        val eventHandler                                            = new DiscordEventHandler[IO]()

        discord
          .subscribe(Shard.singleton, Intent.GuildMessages)
          .mapAsyncUnordered(Int.MaxValue)(handleEvents(eventHandler))
          .compile
          .drain
      }
      .as(ExitCode.Success)
  }

  private def handleEvents(discordEventHandler: DiscordEventHandler[IO]): Event => IO[Unit] = {
    case MessageCreate(BasicMessage(_, content, user, channelId)) if !isBotUser(user) =>
      discordEventHandler.parseAndHandleCommand(channelId, user, content)
    case _ =>
      IO.unit
  }

  private def isBotUser(user: User) = user.bot.getOrElse(false)
}
