package com.wardellbagby.deltas.server.routes.trackers

import com.google.cloud.firestore.DocumentReference
import com.wardellbagby.deltas.server.firebase.ValueWithId
import com.wardellbagby.deltas.server.firebase.asResult
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.firebase.setCatching
import com.wardellbagby.deltas.server.firebase.withId
import com.wardellbagby.deltas.server.helpers.Cache
import com.wardellbagby.deltas.server.helpers.FirebaseAutoUpdatingCache
import com.wardellbagby.deltas.server.helpers.combine
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.model.ServerTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.serializer

typealias Trackers = List<ValueWithId<ServerTracker>>

class TrackersRepository(
  scope: CoroutineScope
) {
  private val trackersCollection = database.collection("trackers")

  private val cache: Cache<String, ValueWithId<ServerTracker>> = FirebaseAutoUpdatingCache(
    valueSerializer = serializer(),
    scope = scope,
    getReferenceFromKey = this::getTrackerReference
  )

  suspend fun getTracker(id: String): Result<ValueWithId<ServerTracker>?> {
    return cache[id]?.asResult()
      ?: getTrackerReference(id)
        .getOrNull<ServerTracker>()
        .onSuccess {
          if (it != null) {
            cache[id] = it
          }
        }
  }

  suspend fun getTrackers(vararg ids: String): Result<Trackers> {
    if (ids.isEmpty()) {
      return Result.success(emptyList())
    }

    val idSet = ids.toSet()
    val cached = (cache.keys intersect idSet)
      .mapNotNull { cache[it] }
    val refsToFetch = (idSet - cache.keys)
      .map(::getTrackerReference)
      .toTypedArray()

    val snapshots = database.getAll(*refsToFetch)
      .awaitCatching()
      .flatMap { snapshots ->
        snapshots
          .map { it.getOrNull<ServerTracker>() }
          // Filter out any trackers that we successfully retrieved but didn't exist
          .filter { it.isFailure || (it.isSuccess && it.getOrNull() != null) }
          .map { it.failIfNull() }
          .combine()
      }
      .fold(
        onSuccess = { it },
        onFailure = { return Result.failure(it) }
      )
      .onEach { (id, value) ->
        cache[id] = ValueWithId(id, value)
      }

    return (cached + snapshots)
      .distinct()
      .sortedWith { left, right ->
        ids.indexOf(left.id).compareTo(ids.indexOf(right.id))
      }
      .let { Result.success(it) }
  }

  private fun getTrackerReference(id: String): DocumentReference {
    return trackersCollection.document(id)
  }

  suspend fun updateTracker(id: String, value: ServerTracker): Result<Unit> {
    return getTrackerReference(id)
      .setCatching(value)
      .onSuccess {
        cache[id] = value.withId(id)
      }
      .onFailure {
        cache.remove(id)
      }
      .map { }
  }

  suspend fun deleteTracker(id: String): Result<Unit> {
    return database.recursiveDelete(trackersCollection.document(id))
      .awaitCatching()
      .map { }
  }
}

suspend fun TrackersRepository.getTrackers(trackerIDs: List<String>) =
  getTrackers(*trackerIDs.toTypedArray())