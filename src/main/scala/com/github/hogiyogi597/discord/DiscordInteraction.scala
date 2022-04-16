package com.github.hogiyogi597.discord

import cats.effect.IO
import dissonance.DiscordClient
import dissonance.DiscordClient.WebhookMessage
import dissonance.data._

trait DiscordInteraction[F[_]] {
  def sendMessage(channelId: Snowflake, message: String): F[Unit]
  def deleteMessage(channelId: Snowflake, messageId: Snowflake): F[Unit]
  def sendEmbeddedMessage(channelId: Snowflake, user: User, embeddedMessages: List[Embed]): F[(Webhook, Message)]
}

object DiscordInteraction {
  def apply[F[_]](implicit DiscordInteraction: DiscordInteraction[F]): DiscordInteraction[F] = DiscordInteraction
}

class DiscordClientInteraction(discordClient: DiscordClient) extends DiscordInteraction[IO] {
  override def sendMessage(channelId: Snowflake, message: String): IO[Unit] =
    discordClient.sendMessage(message, channelId).void

  override def deleteMessage(channelId: Snowflake, messageId: Snowflake): IO[Unit] =
    discordClient.deleteMessage(channelId, messageId)

  override def sendEmbeddedMessage(channelId: Snowflake, user: User, embeddedMessages: List[Embed]): IO[(Webhook, Message)] =
    for {
      webhook <- discordClient.createWebhook("ByteMeSearchResults", None, channelId)
      message <- discordClient.executeWebhookWithResponse(
                   webhook,
                   WebhookMessage(s"<@${user.id}> Type the number of the sound byte you want or 'cancel'", None, None, None, (), embeddedMessages, (), ())
                 )
      _ <- discordClient.deleteWebhook(webhook.id)
    } yield (webhook, message)
}
