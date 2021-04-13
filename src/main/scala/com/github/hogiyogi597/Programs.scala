package com.github.hogiyogi597

import cats.free.Free
import cats.implicits._
import com.github.hogiyogi597.discord.createEmbeddedMessage
import com.github.hogiyogi597.discord.DiscordInteraction
import com.github.hogiyogi597.persistence.UserInteractionStore
import com.github.hogiyogi597.persistence.UserInteractionStore.UserSearchState
import com.github.hogiyogi597.yarn.Yarn
import dissonance.data.{Snowflake, User}

object Programs {
  def getRandom[S[_]](channelId: Snowflake)(implicit yarn: Yarn.Ops[S], discord: DiscordInteraction.Ops[S]): Free[S, Unit] = {
    for {
      yarnResult <- yarn.getPopular
      _          <- yarnResult.traverse(result => discord.sendMessage(channelId, result.url.renderString))
    } yield ()
  }

  def getFromSearchString[S[_]](channelId: Snowflake, searchPhrase: String)(implicit yarn: Yarn.Ops[S], discord: DiscordInteraction.Ops[S]): Free[S, Unit] = {
    for {
      yarnResult <- yarn.searchTerm(searchPhrase)
      _          <- yarnResult.traverse(result => discord.sendMessage(channelId, result.url.renderString))
    } yield ()
  }

  def getMultiSearchResults[S[_]](channelId: Snowflake, user: User, searchPhrase: String)(implicit
      yarn: Yarn.Ops[S],
      userSearchStore: UserInteractionStore.Ops[S],
      discord: DiscordInteraction.Ops[S]
  ): Free[S, Unit] = {
    for {
      yarnResults            <- yarn.multiSearchTerm(searchPhrase)
      discordEmbeddedMessages = yarnResults.zip(LazyList.from(1)).map { case (yarnResult, index) => createEmbeddedMessage(yarnResult, index) }
      webhookId              <- discord.sendEmbeddedMessage(channelId, user, discordEmbeddedMessages)
      _                      <- userSearchStore.startUserSearch(user.id.value, UserSearchState(yarnResults, webhookId))
    } yield ()
  }

  // TODO: Make this Int a refined type for 1-10
  def completeMultiSearchCommand[S[_]](channelId: Snowflake, userId: Snowflake, selectedIndex: Int)(implicit
      userSearchStore: UserInteractionStore.Ops[S],
      discord: DiscordInteraction.Ops[S]
  ): Free[S, Unit] = {
    for {
      maybeUserSearchState <- userSearchStore.completeUserSearch(userId)
      maybeWebhookAndYarnResult = maybeUserSearchState.flatMap { case UserSearchState(yarnResults, webhookId) =>
                                    yarnResults.get(selectedIndex - 1L).map(webhookId -> _)
                                  }
      _ <- maybeWebhookAndYarnResult.traverse { case (webhookId, yarnResult) =>
             discord.sendMessage(channelId, yarnResult.url.renderString) *> discord.deleteWebhook(webhookId)
           }
    } yield ()
  }

  def cancelMultiSearchCommand[S[_]](userId: Snowflake)(implicit
      userSearchStore: UserInteractionStore.Ops[S],
      discord: DiscordInteraction.Ops[S]
  ): Free[S, Unit] = {
    for {
      maybeUserSearchState <- userSearchStore.cancelUserSearch(userId)
      _                    <- maybeUserSearchState.traverse(userSearchState => discord.deleteWebhook(userSearchState.webhookId))
    } yield ()
  }
}
