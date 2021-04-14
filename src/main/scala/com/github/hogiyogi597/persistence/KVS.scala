package com.github.hogiyogi597.persistence

import cats.effect.Sync
import cats.effect.concurrent.Ref

trait KVS[F[_]] {
  def get(key: Long): F[Option[UserSearchState]]
  def put(key: Long, userSearchState: UserSearchState): F[Unit]
  def delete(key: Long): F[Unit]
}

object KVS {
  def apply[F[_]](implicit KVS: KVS[F]): KVS[F] = KVS
}

class LocalUserSearchStoreKVS[F[_]: Sync] private (ref: Ref[F, Map[Long, UserSearchState]]) extends KVS[F] {
  override def get(key: Long): F[Option[UserSearchState]]                = Sync[F].map(ref.get)(_.get(key))
  override def put(key: Long, userSearchState: UserSearchState): F[Unit] = ref.update(_ + (key -> userSearchState))
  override def delete(key: Long): F[Unit]                                = ref.update(_ - key)
}

object LocalUserSearchStoreKVS {
  def make[F[_]: Sync]: F[LocalUserSearchStoreKVS[F]] =
    Sync[F].map(Ref.of(Map.empty[Long, UserSearchState]))(new LocalUserSearchStoreKVS[F](_))
}
