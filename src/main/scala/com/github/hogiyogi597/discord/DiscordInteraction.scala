package com.github.hogiyogi597.discord

import cats.effect.IO
import cats.free.Free
import cats.{InjectK, ~>}
import dissonance.DiscordClient
import dissonance.DiscordClient.WebhookMessage
import dissonance.data.{Embed, Snowflake, User}

sealed trait DiscordInteraction[A]
case class SendMessage(channelId: Snowflake, message: String)                                   extends DiscordInteraction[Unit]
case class SendEmbeddedMessage(channelId: Snowflake, user: User, embeddedMessages: List[Embed]) extends DiscordInteraction[Snowflake]
case class DeleteWebhook(webhookId: Snowflake)                                                  extends DiscordInteraction[Unit]

object DiscordInteraction {
  class Ops[F[_]](implicit I: InjectK[DiscordInteraction, F]) {
    def sendMessage(channelId: Snowflake, message: String): Free[F, Unit] = Free.liftInject(SendMessage(channelId, message))
    def sendEmbeddedMessage(channelId: Snowflake, user: User, embeddedMessages: List[Embed]): Free[F, Snowflake] =
      Free.liftInject(SendEmbeddedMessage(channelId, user, embeddedMessages))
    def deleteWebhook(webhookId: Snowflake): Free[F, Unit] = Free.liftInject(DeleteWebhook(webhookId))
  }

  object Ops {
    implicit def discordInteractionOps[F[_]](implicit I: InjectK[DiscordInteraction, F]): Ops[F] = new Ops[F]()
  }
}

object DiscordClientInterpreter {
  def interpreter(discordClient: DiscordClient): DiscordInteraction ~> IO = new (DiscordInteraction ~> IO) {
    override def apply[A](fa: DiscordInteraction[A]): IO[A] = fa match {
      case SendMessage(channelId, message) =>
        discordClient.sendMessage(message, channelId).void
      case SendEmbeddedMessage(channelId, user, embeddedMessages) =>
        for {
          webhook <- discordClient.createWebhook("ByteMeSearchResults", None, channelId)
          message <- discordClient.executeWebhookWithResponse(
                       webhook,
                       WebhookMessage(s"<@${user.id}> Type the number of the sound byte you want or 'cancel'", None, None, None, (), embeddedMessages, (), ())
                     )
          _ <- discordClient.deleteWebhook(webhook.id)
        } yield webhook.id
      case DeleteWebhook(webhookId) =>
        discordClient.deleteWebhook(webhookId).void
    }
  }
}
