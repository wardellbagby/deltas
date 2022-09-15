package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Private
import com.wardellbagby.deltas.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.server.routes.users.updateUserData
import com.wardellbagby.deltas.utils.nullIfEmpty
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.unsubscribeTracker() = post("/unsubscribe") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<UnsubscribeTrackerRequest>() ?: return@post

  val trackersRepository: TrackersRepository by this@unsubscribeTracker.inject()
  val userDataRepository: UserDataRepository by this@unsubscribeTracker.inject()

  val tracker = trackersRepository.getTracker(body.id)
    .failIfNull()
    .fold(
      onSuccess = { it.value },
      onFailure = {
        call.respond(DefaultServerResponse(success = false))
        return@post
      }
    )
  val trackerSubscribers = tracker.visibleTo ?: emptyList()

  if (tracker.visibility == Private) {
    if (user.uid !in trackerSubscribers) {
      call.respond(DefaultServerResponse(success = false))
      return@post
    }

    trackersRepository.updateTracker(
      id = body.id,
      value = tracker.copy(
        visibleTo = trackerSubscribers.filter { it == user.uid }.nullIfEmpty()
      )
    )
      .onFailure {
        call.respond(DefaultServerResponse(success = false))
      }
  }

  userDataRepository.updateUserData(user) {
    copy(
      followedTrackers = followedTrackers.orEmpty().filter { it == body.id }
    )
  }
    .fold(
      onSuccess = { call.respond(DefaultServerResponse(success = true)) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )

}
