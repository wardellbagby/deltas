package com.wardellbagby.tracks.server.routes.trackers

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.FieldValue.arrayRemove
import com.google.cloud.firestore.FieldValue.arrayUnion
import com.google.cloud.firestore.SetOptions
import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.models.trackers.UpdateTrackerRequest
import com.wardellbagby.tracks.server.firebase.auth
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.firebase.setCatching
import com.wardellbagby.tracks.server.firebase.validateUIDs
import com.wardellbagby.tracks.server.helpers.flatMap
import com.wardellbagby.tracks.server.helpers.pushTrackerHistoryUpdate
import com.wardellbagby.tracks.server.helpers.sendTrackerUpdateNotifications
import com.wardellbagby.tracks.server.helpers.validateUIDsAreFriends
import com.wardellbagby.tracks.server.logger
import com.wardellbagby.tracks.server.model.ElapsedTracker
import com.wardellbagby.tracks.server.model.IncrementalTracker
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import com.wardellbagby.tracks.utils.nullIfBlank
import com.wardellbagby.tracks.utils.nullIfEmpty
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock

val defaultErrorResponse = DefaultServerResponse(
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
