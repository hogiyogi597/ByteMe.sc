package com.github.hogiyogi597.persistence

import cats.effect.{IO, Resource}
import com.github.hogiyogi597.yarn.YarnResult
import org.http4s.Uri
import weaver._

object KVSTest extends IOSuite {
  override type Res = LocalUserSearchStoreKVS[IO]

  override def sharedResource: Resource[IO, Res] = Resource.eval(LocalUserSearchStoreKVS.make[IO])

  test(".get on empty kvs should not return anything") { kvs =>
    val key = 1L
    for {
      maybeUserSearchState <- kvs.get(key)
    } yield expect(maybeUserSearchState.isEmpty)
  }

  test(".put should store object") { kvs =>
    val key             = 1L
    val userSearchState = UserSearchState(List(YarnResult(Uri(), Uri(), "title", "transcript", 1.0)), 123L, 1234L)
    for {
      _                    <- kvs.put(key, userSearchState)
      maybeUserSearchState <- kvs.get(key)
    } yield expect(maybeUserSearchState.get == userSearchState)
  }

  test(".delete should remove object") { kvs =>
    val key             = 1L
    val userSearchState = UserSearchState(List(YarnResult(Uri(), Uri(), "title", "transcript", 1.0)), 123L, 1234L)
    for {
      _                   <- kvs.put(key, userSearchState)
      foundSearchState    <- kvs.get(key)
      _                   <- kvs.delete(key)
      notFoundSearchState <- kvs.get(key)
    } yield expect(foundSearchState.get == userSearchState) and expect(notFoundSearchState.isEmpty)
  }

}
