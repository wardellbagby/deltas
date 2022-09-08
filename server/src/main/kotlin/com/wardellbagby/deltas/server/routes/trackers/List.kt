package com.wardellbagby.deltas.server.routes.trackers

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Query.Direction.DESCENDING
import com.google.firebase.auth.UserRecord
import com.wardellbagby.deltas.models.trackers.ListTrackersRequest
import com.wardellbagby.deltas.models.trackers.ListTrackersResponse
import com.wardellbagby.deltas.models.trackers.TrackerDTO
import com.wardellbagby.deltas.server.firebase.ValueWithId
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrEmpty
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.helpers.combine
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.helpers.getFollowedTrackers
import com.wardellbagby.deltas.server.logger
import com.wardellbagby.deltas.server.model.ServerTracker
import com.wardellbagby.deltas.server.model.toDTO
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

private suspend fun UserRecord.getSelfTrackers(
  cursorSnapshot: DocumentSnapshot?,
  limit: Int
): Result<List<ValueWithId<ServerTracker>>> {
  return database.collection("trackers")
    .orderBy("timestamp", DESCENDING)
    .whereEqualTo("creator", uid)
    .let {
      if (cursorSnapshot != null) {
        it.startAfter(cursorSnapshot)
      } else {
        it
      }
    }
    .limit(limit)
    .getOrEmpty()
}

private suspend fun UserRecord.getOtherTrackers(
  cursorSnapshot: DocumentSnapshot?,
  limit: Int
): Result<List<ValueWithId<ServerTracker>>> {
  val followedTrackers = getFollowedTrackers()

  val cursorId = cursorSnapshot?.id

  // Firestore is...very annoying and doesn't allow us to both sort a query by creator and timestamp
  // AND enforce that every item returns matches an ID. Yes, very annoying I know. So instead, we do
  // this extremely optimal thing by getting every single tracker the user is following and doing
  // the sorting and filtering ourselves. I hate Firestore.
  return database.getAll(*followedTrackers.toTypedArray())
    .awaitCatching()
    .flatMap { snapshots ->
      snapshots
        .map { snapshot ->
          snapshot.reference.getOrNull<ServerTracker>().failIfNull()
        }
        .combine()
        .map { otherTrackers ->
          otherTrackers
            .sortedWith(
              compareBy(
                { it.value.creator },
                { it.value.timestamp }
              )
            )
        }
        .map {
          val result = if (cursorId != null) {
            val cursorIndex = it.indexOfFirst { tracker -> tracker.id == cursorId }
            it.drop(cursorIndex + 1)
          } else {
            it
          }
          result.take(limit)
        }
    }
}

private const val SELF_TRACKER_CURSOR_PREFIX = "self-trackers:"
private const val SHARED_TRACKER_CURSOR_PREFIX = "shared-trackers:"
val String?.isSelfTrackerCursor: Boolean
  get() = this == null || startsWith(SELF_TRACKER_CURSOR_PREFIX)

val String?.isSharedTrackerCursor: Boolean
  get() = this == null || startsWith(SHARED_TRACKER_CURSOR_PREFIX)

val String.asDocumentId: String
  get() = removePrefix(SELF_TRACKER_CURSOR_PREFIX)
    .removePrefix(SHARED_TRACKER_CURSOR_PREFIX)

val TrackerDTO.asCursor: String
  get() = if (owner.isSelf) "self-trackers:$id" else "shared-trackers:$id"

fun Route.listTrackers() = post("/list") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<ListTrackersRequest>() ?: return@post
  val limit = minOf(body.limit ?: 20, 20)

  val cursorSnapshot = body.cursor?.let {
    database.collection("trackers")
      .document(it.asDocumentId)
      .get()
      .awaitCatching()
      .getOrNull()
  }

  val selfTrackers = if (body.cursor.isSelfTrackerCursor) {
    user.getSelfTrackers(
      cursorSnapshot = cursorSnapshot,
      limit = limit
    )
  } else {
    Result.success(emptyList())
  }
  val otherTrackers = selfTrackers.flatMap {
    if (it.size < limit) {
      user.getOtherTrackers(
        cursorSnapshot = cursorSnapshot.takeIf { body.cursor.isSharedTrackerCursor },
        limit = limit - it.size
      )
    } else {
      Result.success(emptyList())
    }
  }

  val trackers = selfTrackers
    .flatMap {
      if (otherTrackers.isSuccess) {
        Result.success(it + otherTrackers.getOrThrow())
      } else {
        otherTrackers
      }
    }
    .onFailure {
      logger.error("Error loading trackers", it)
      call.respond(ListTrackersResponse(success = false))
      return@post
    }
    .getOrThrow()
    .map { (id, tracker) ->
      tracker.toDTO(
        id = id,
        selfUID = user.uid,
        isUserSubscribed = true
      )
    }

  call.respond(
    ListTrackersResponse(
      trackers = trackers,
      cursor = trackers.lastOrNull()?.asCursor
    )
  )
}