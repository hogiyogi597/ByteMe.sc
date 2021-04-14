package com.github.hogiyogi597.persistence

import cats.FlatMap
import cats.syntax.flatMap._
import cats.syntax.functor._
import dissonance.data.Snowflake

trait UserInteractionStore[F[_]] {
  def getUserSearchState(id: Snowflake): F[Option[UserSearchState]]
  def startUserSearch(id: Snowflake, userSearchState: UserSearchState): F[Unit]
  def cancelUserSearch(id: Snowflake): F[Option[UserSearchState]]
  def completeUserSearch(id: Snowflake): F[Option[UserSearchState]]
}

object UserInteractionStore {
  def apply[F[_]](implicit UserInteractionStore: UserInteractionStore[F]): UserInteractionStore[F] = UserInteractionStore
}

class UserInteractionStoreInterpreter[F[_]: FlatMap: KVS] extends UserInteractionStore[F] {
  override def getUserSearchState(id: Snowflake): F[Option[UserSearchState]] = KVS[F].get(id)

  override def startUserSearch(id: Snowflake, userSearchState: UserSearchState): F[Unit] = KVS[F].put(id, userSearchState)

  override def cancelUserSearch(id: Snowflake): F[Option[UserSearchState]] = getAndDelete(id)

  override def completeUserSearch(id: Snowflake): F[Option[UserSearchState]] = getAndDelete(id)

  private def getAndDelete(id: Snowflake): F[Option[UserSearchState]] = for {
    result <- KVS[F].get(id)
    _      <- KVS[F].delete(id)
  } yield result
}
