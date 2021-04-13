package com.github.hogiyogi597.persistence

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.free.Free
import cats.{InjectK, ~>}
import dissonance.data.Snowflake
import com.github.hogiyogi597.persistence.UserInteractionStore.UserSearchState
import com.github.hogiyogi597.yarn.YarnResult

// TODO: Change this to be a generic KVS
sealed trait UserInteractionStore[A]
case class Get(id: Long)                                   extends UserInteractionStore[Option[UserSearchState]]
case class Set(id: Long, userSearchState: UserSearchState) extends UserInteractionStore[Unit]
case class Delete(id: Long)                                extends UserInteractionStore[Option[UserSearchState]]

object UserInteractionStore {
  // TODO: We need to delete the message after the user cancels or selects
  case class UserSearchState(results: List[YarnResult], webhookId: Snowflake)

  class Ops[F[_]](implicit I: InjectK[UserInteractionStore, F]) {
    private def delete(id: Long): Free[F, Option[UserSearchState]]                 = Free.liftInject(Delete(id))
    def getUserSearchState(id: Long): Free[F, Option[UserSearchState]]             = Free.liftInject(Get(id))
    def startUserSearch(id: Long, userSearchState: UserSearchState): Free[F, Unit] = Free.liftInject(Set(id, userSearchState))
    def cancelUserSearch(id: Long): Free[F, Option[UserSearchState]]               = delete(id)
    def completeUserSearch(id: Long): Free[F, Option[UserSearchState]] = for {
      userSearchState <- getUserSearchState(id)
      _               <- delete(id)
    } yield userSearchState
  }

  object Ops {
    implicit def userInteractionStoreOps[F[_]](implicit I: InjectK[UserInteractionStore, F]): Ops[F] = new Ops()
  }
}

class LocalUserInteractionStateStoreInterpreter[F[_]: Sync] private (ref: Ref[F, Map[Long, UserSearchState]]) {
  val kvs: Map[Long, UserSearchState] = Map[Long, UserSearchState]()

  def interpreter: UserInteractionStore ~> F = new (UserInteractionStore ~> F) {
    override def apply[A](fa: UserInteractionStore[A]): F[A] = fa match {
      case Get(id)                  => Sync[F].map(ref.get)(_.get(id))
      case Set(id, userSearchState) => Sync[F].void(ref.getAndUpdate(_.updated(id, userSearchState)))
      case Delete(id)               => Sync[F].map(ref.getAndUpdate(_.removed(id)))(_.get(id))
    }
  }
}

object LocalUserInteractionStateStoreInterpreter {
  def make[F[_]](implicit sync: Sync[F]): F[LocalUserInteractionStateStoreInterpreter[F]] = {
    sync.map(Ref.of[F, Map[Long, UserSearchState]](Map.empty))(ref => new LocalUserInteractionStateStoreInterpreter(ref))
  }
}
