package com.wardellbagby.tracks.server.routes.trackers

import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.models.trackers.UpdateTrackerRequest
import com.wardellbagby.tracks.server.firebase.auth
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.firebase.setCatching
import com.wardellbagby.tracks.server.firebase.validateUIDs
import com.wardellbagby.tracks.server.helpers.pushTrackerHistoryUpdate
import com.wardellbagby.tracks.server.helpers.sendTrackerUpdateNotifications
import com.wardellbagby.tracks.server.helpers.validateUIDsAreFriends
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

      IncrementalTracker(
        label = tracker.label,
        creator = tracker.creator,
        visibleTo = visibleTo,
        count = if (body.shouldIncrementCount) {
          tracker.count + 1
        } else {
          tracker.count
        }
      )
    }
  }

  trackerRef.setCatching(updatedTracker)
    .fold(
      onSuccess = { call.respond(DefaultServerResponse(success = true)) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
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
