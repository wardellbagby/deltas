package com.wardellbagby.deltas.server.routes.users

import com.google.cloud.firestore.DocumentReference
import com.google.firebase.auth.UserRecord
import com.wardellbagby.deltas.server.firebase.ValueWithId
import com.wardellbagby.deltas.server.firebase.asResult
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.firebase.setCatching
import com.wardellbagby.deltas.server.firebase.withId
import com.wardellbagby.deltas.server.helpers.Cache
import com.wardellbagby.deltas.server.helpers.FirebaseAutoUpdatingCache
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.model.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.serializer

class UserDataRepository(
  scope: CoroutineScope
) {
  private val userCollection = database.collection("users")
  private val cache: Cache<String, ValueWithId<UserData>> = FirebaseAutoUpdatingCache(
    valueSerializer = serializer(),
    scope = scope,
    getReferenceFromKey = this::getUserDataReference
  )

  suspend fun getUserData(id: String): Result<ValueWithId<UserData>?> {
    return cache[id]?.asResult() ?: userCollection
      .document(id)
      .getOrNull<UserData>()
      .onSuccess {
        if (it != null) {
          cache[id] = it
        }
      }
  }

  suspend fun setUserData(id: String, data: UserData): Result<Unit> {
    return database.collection("users")
      .document(id)
      .setCatching(data)
      .map { }
      .onSuccess {
        cache[id] = data.withId(id)
      }
      .onFailure {
        cache.remove(id)
      }
  }

  suspend fun updateUserData(
    id: String,
    block: UserData.() -> UserData
  ): Result<Unit> {
    return getUserData(id)
      .mapCatching {
        block(
          it?.value ?: UserData(
            messageToken = null,
            createdTrackers = null,
            followedTrackers = null
          )
        )
      }
      .flatMap { userData ->
        setUserData(id, userData)
          .map { userData to it }
      }
      .onSuccess { (data, _) ->
        cache[id] = data.withId(id)
      }
      .onFailure {
        cache.remove(id)
      }
      .map { }
  }

  private fun getUserDataReference(id: String): DocumentReference {
    return userCollection.document(id)
  }
}

suspend fun UserDataRepository.getUserData(record: UserRecord) =
  getUserData(record.uid).map { it?.value }

suspend fun UserDataRepository.setUserData(
  record: UserRecord,
  data: UserData
) = setUserData(record.uid, data)

suspend fun UserDataRepository.updateUserData(
  record: UserRecord,
  block: UserData.() -> UserData
) = updateUserData(record.uid) { block() }