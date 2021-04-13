package com.github.hogiyogi597

import cats.effect.{ExitCode, IO, IOApp}
import com.github.hogiyogi597.discord.DiscordEventHandler
import dissonance.Discord
import dissonance.data.{Intent, Shard}
import org.http4s.client.Client
import org.http4s.client.middleware.Logger

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val discordToken = args(0)
    Discord
      .make(discordToken)
      .use { case (discord) =>
        val eventHandler = new DiscordEventHandler(discord.client)

        discord
          .subscribe(Shard.singleton, Intent.GuildMessages)
          .evalMap(eventHandler.handleEvent)
          .compile
          .drain
      }
      .as(ExitCode.Success)
  }
}

// TODO: Things are running really slow... Figure out why and speed it up
