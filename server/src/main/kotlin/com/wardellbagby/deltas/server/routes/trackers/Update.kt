package com.wardellbagby.deltas.server.routes.trackers

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.FieldValue.arrayRemove
import com.google.cloud.firestore.FieldValue.arrayUnion
import com.google.cloud.firestore.SetOptions
import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.models.trackers.UpdateTrackerRequest
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.firebase.setCatching
import com.wardellbagby.deltas.server.firebase.validateUIDs
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.helpers.pushTrackerHistoryUpdate
import com.wardellbagby.deltas.server.helpers.sendTrackerUpdateNotifications
import com.wardellbagby.deltas.server.helpers.validateUIDsAreFriends
import com.wardellbagby.deltas.server.logger
import com.wardellbagby.deltas.server.model.ElapsedTracker
import com.wardellbagby.deltas.server.model.IncrementalTracker
import com.wardellbagby.deltas.server.model.ServerTracker
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.utils.nullIfBlank
import com.wardellbagby.deltas.utils.nullIfEmpty
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock

private val defaultErrorResponse = DefaultServerResponse(
  success = false,
  errorDetailMessage = "Failed to update tracker"
)

suspend fun List<String>.isAllowedForSharing(oldIds: List<String>, selfUID: String): Boolean {
  return auth.validateUIDs(this) &&
      validateUIDsAreFriends(selfUID = selfUID, ids = this - oldIds.toSet())
}

fun Route.updateTrackers() = post("/update") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<UpdateTrackerRequest>() ?: return@post

  val trackerRef = database
    .collection("trackers")
    .document(body.id)

  val tracker = trackerRef.getOrNull<ServerTracker>()
    .mapCatching { it!! }
    .onFailure {
      call.respond(defaultErrorResponse)
      return@post
    }
    .getOrThrow()
    .value

  if (tracker.creator != user.uid) {
    call.respond(defaultErrorResponse)
    return@post
  }

  val idsToShareWith = body.idsToShareWith?.distinct()?.take(10)
  if (idsToShareWith != null && !idsToShareWith.isAllowedForSharing(
      oldIds = tracker.visibleTo ?: emptyList(), selfUID = user.uid
    )
  ) {
    call.respond(defaultErrorResponse)
    return@post
  }

  val visibleTo = (idsToShareWith ?: tracker.visibleTo)?.distinct()?.nullIfEmpty()
  val updatedTracker: ServerTracker = when (tracker.type) {
    Elapsed -> {
      if (tracker.resetTime == null) {
        call.respond(defaultErrorResponse)
        return@post
      }

      ElapsedTracker(
        label = tracker.label,
        creator = tracker.creator,
        visibleTo = visibleTo,
        visibility = tracker.visibility,
        resetTime = if (body.shouldResetTime) {
          Clock.System.now()
        } else {
          tracker.resetTime
        }
      )
    }

    Incremental -> {
      if (tracker.count == null) {
        call.respond(defaultErrorResponse)
        return@post
      }
      val (count, timestamp) = if (body.shouldIncrementCount) {
        (tracker.count + 1) to Clock.System.now()
      } else {
        tracker.count to tracker.timestamp
      }

      IncrementalTracker(
        label = tracker.label,
        creator = tracker.creator,
        visibleTo = visibleTo,
        visibility = tracker.visibility,
        count = count,
        timestamp = timestamp
      )
    }
  }

  trackerRef
    .setCatching(updatedTracker)
    .flatMap {
      updateCreatedTracker(
        selfId = user.uid,
        trackerRef = trackerRef
      )
    }
    .flatMap {
      updateSharedTracker(
        old = tracker,
        new = updatedTracker,
        trackerRef = trackerRef
      )
    }
    .fold(
      onSuccess = {
        call.respond(DefaultServerResponse(success = true))
      },
      onFailure = {
        logger.error("Failed to update tracker", it)
        call.respond(DefaultServerResponse(success = false))
      }
    )

  if (body.shouldResetTime || body.shouldIncrementCount) {
    sendTrackerUpdateNotifications(body.id)
    pushTrackerHistoryUpdate(
      trackerId = body.id,
      oldTracker = tracker,
      newTracker = updatedTracker,
      label = body.label?.nullIfBlank()
    )
  }
}

private suspend fun updateCreatedTracker(
  selfId: String,
  trackerRef: DocumentReference
): Result<Unit> {
  return database.collection("users")
    .document(selfId)
    .set(mapOf("createdTrackers" to arrayUnion(trackerRef)), SetOptions.merge())
    .awaitCatching()
    .map {
      println(it)
    }
}

private suspend fun updateSharedTracker(
  old: ServerTracker,
  new: ServerTracker,
  trackerRef: DocumentReference,
): Result<Unit> {
  val oldVisibleTo = old.visibleTo.orEmpty().toSet()
  val newVisibleTo = new.visibleTo.orEmpty().toSet()

  if (oldVisibleTo == newVisibleTo) {
    return Result.success(Unit)
  }

  val newlyVisibleTo = newVisibleTo - oldVisibleTo
  val notVisibleTo = oldVisibleTo - newlyVisibleTo

  val writer = database.bulkWriter()
  newlyVisibleTo.forEach { userId ->
    writer.set(
      database.collection("users").document(userId),
      mapOf("followedTrackers" to arrayUnion(trackerRef)),
      SetOptions.merge()
    )
  }

  notVisibleTo.forEach { userId ->
    writer.set(
      database.collection("users").document(userId),
      mapOf("followedTrackers" to arrayRemove(trackerRef)),
      SetOptions.merge()
    )
  }

  return writer.flush().awaitCatching().map { }
}
