package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.models.trackers.UpdateTrackerRequest
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.firebase.validateUIDs
import com.wardellbagby.deltas.server.helpers.combine
import com.wardellbagby.deltas.server.helpers.failIfNull
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
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.utils.nullIfBlank
import com.wardellbagby.deltas.utils.nullIfEmpty
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject

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

  val trackersRepository: TrackersRepository by this@updateTrackers.inject()
  val userDataRepository: UserDataRepository by this@updateTrackers.inject()

  val trackerId = body.id

  val tracker = trackersRepository.getTracker(trackerId)
    .failIfNull()
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

  trackersRepository.updateTracker(trackerId, updatedTracker)
    .flatMap {
      updateCreatedTracker(
        selfId = user.uid,
        trackerId = trackerId,
        userDataRepository = userDataRepository
      )
    }
    .flatMap {
      updateSharedTracker(
        old = tracker,
        new = updatedTracker,
        trackerId = trackerId,
        userDataRepository = userDataRepository
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
    sendTrackerUpdateNotifications(
      trackersRepository = trackersRepository,
      trackerId = trackerId
    )
    pushTrackerHistoryUpdate(
      trackerId = trackerId,
      oldTracker = tracker,
      newTracker = updatedTracker,
      label = body.label?.nullIfBlank()
    )
  }
}

private suspend fun updateCreatedTracker(
  selfId: String,
  trackerId: String,
  userDataRepository: UserDataRepository
): Result<Unit> {
  return userDataRepository.updateUserData(selfId) {
    copy(createdTrackers = createdTrackers.orEmpty() + trackerId)
  }
}

private suspend fun updateSharedTracker(
  old: ServerTracker,
  new: ServerTracker,
  trackerId: String,
  userDataRepository: UserDataRepository
): Result<Unit> {
  val oldVisibleTo = old.visibleTo.orEmpty().toSet()
  val newVisibleTo = new.visibleTo.orEmpty().toSet()

  if (oldVisibleTo == newVisibleTo) {
    return Result.success(Unit)
  }

  val newlyVisibleTo = newVisibleTo - oldVisibleTo
  val notVisibleTo = oldVisibleTo - newlyVisibleTo

  val addedResults = newlyVisibleTo.map { userId ->
    userDataRepository.updateUserData(userId) {
      copy(followedTrackers = followedTrackers.orEmpty() + trackerId)
    }
  }

  val removedResults = notVisibleTo.map { userId ->
    userDataRepository.updateUserData(userId) {
      copy(followedTrackers = followedTrackers.orEmpty().filter { it == trackerId })
    }
  }

  return (addedResults + removedResults).combine().map { }
}
