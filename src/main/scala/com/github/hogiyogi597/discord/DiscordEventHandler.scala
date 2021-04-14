package com.github.hogiyogi597.discord

import cats.Monad
import cats.syntax.all._
import com.github.hogiyogi597.models.ParsedCommand._
import com.github.hogiyogi597.persistence.{UserInteractionStore, UserSearchState}
import com.github.hogiyogi597.yarn.Yarn
import dissonance.data.{Snowflake, User}

class DiscordEventHandler[F[_]: Monad: Yarn: DiscordInteraction: UserInteractionStore: MessageParser] {
  def parseAndHandleCommand(channelId: Snowflake, user: User, rawInputMessage: String): F[Unit] = {
    for {
      parsedCommand <- MessageParser[F].parseMessage(rawInputMessage)
      _ <- parsedCommand.traverse {
             case HelpCommand() => DiscordInteraction[F].sendMessage(channelId, helpMessage)
             case RandomSearchCommand() =>
               for {
                 yarnResult <- Yarn[F].getPopular
                 _          <- yarnResult.traverse(result => DiscordInteraction[F].sendMessage(channelId, result.url.renderString))
               } yield ()
             case SingleSearchCommand(searchPhrase) =>
               for {
                 yarnResult <- Yarn[F].searchTerm(searchPhrase)
                 _          <- yarnResult.traverse(result => DiscordInteraction[F].sendMessage(channelId, result.url.renderString))
               } yield ()
             case MultiSearchCommand(searchPhrase) =>
               for {
                 yarnResults            <- Yarn[F].multiSearchTerm(searchPhrase)
                 discordEmbeddedMessages = yarnResults.zip(LazyList.from(1)).map { case (yarnResult, index) => createEmbeddedMessage(yarnResult, index) }
                 (webhook, message)     <- DiscordInteraction[F].sendEmbeddedMessage(channelId, user, discordEmbeddedMessages)
                 _                      <- UserInteractionStore[F].startUserSearch(user.id.value, UserSearchState(yarnResults, webhook.id, message.id))
               } yield ()
             case CompleteMultiSearchCommand(selectedIndex) =>
               for {
                 maybeUserSearchState <- UserInteractionStore[F].completeUserSearch(user.id.value)
                 maybeYarnResult = maybeUserSearchState.flatMap { case UserSearchState(yarnResults, webhookId, messageId) =>
                                     yarnResults.get(selectedIndex - 1L).map(yarnResult => (yarnResult, webhookId, messageId))
                                   }
                 _ <- maybeYarnResult.traverse { case (yarnResult, _, messageId) =>
                        DiscordInteraction[F].sendMessage(channelId, yarnResult.url.renderString) *>
                          DiscordInteraction[F].deleteMessage(channelId, messageId)
                      }
               } yield ()
             case CancelMultiSearchCommand() =>
               for {
                 maybeUserSearchState <- UserInteractionStore[F].cancelUserSearch(user.id.value)
                 _ <- maybeUserSearchState.traverse { case UserSearchState(_, _, messageId) =>
                        DiscordInteraction[F].deleteMessage(channelId, messageId)
                      }
               } yield ()
           }
    } yield ()
  }
}
