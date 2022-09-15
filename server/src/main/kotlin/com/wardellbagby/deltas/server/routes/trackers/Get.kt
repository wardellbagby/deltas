package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.trackers.GetTrackerRequest
import com.wardellbagby.deltas.models.trackers.GetTrackerResponse
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Public
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.model.toDTO
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.server.routes.users.getUserData
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

private val defaultErrorResponse = GetTrackerResponse(
  tracker = null,
  success = false,
  errorDetailMessage = "This tracker does not exist"
)

fun Route.getTracker() = post("/get") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<GetTrackerRequest>() ?: return@post

  val trackersRepository: TrackersRepository by this@getTracker.inject()
  val userDataRepository: UserDataRepository by this@getTracker.inject()

  val followedTrackers = userDataRepository.getUserData(user)
    .onFailure {
      call.respond(defaultErrorResponse)
      return@post
    }
    .getOrNull()
    ?.followedTrackers
    ?: emptyList()

  val (id, tracker) = trackersRepository.getTracker(body.id)
    .failIfNull()
    .onFailure {
      call.respond(defaultErrorResponse)
      return@post
    }
    .getOrThrow()

  if (tracker.visibility != Public) {
    call.respond(defaultErrorResponse)
    return@post
  }

  call.respond(
    GetTrackerResponse(
      tracker = tracker.toDTO(
        id = id,
        selfUID = user.uid,
        isUserSubscribed = followedTrackers.contains(id)
      )
    )
  )
}