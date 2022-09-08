package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.trackers.GetTrackerRequest
import com.wardellbagby.deltas.models.trackers.GetTrackerResponse
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Public
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.helpers.getFollowedTrackers
import com.wardellbagby.deltas.server.model.ServerTracker
import com.wardellbagby.deltas.server.model.toDTO
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

private val defaultErrorResponse = GetTrackerResponse(
  tracker = null,
  success = false,
  errorDetailMessage = "This tracker does not exist"
)

fun Route.getTracker() = post("/get") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<GetTrackerRequest>() ?: return@post

  val followedTrackers = user.getFollowedTrackers().map { it.id }

  val (id, tracker) = database.collection("trackers")
    .document(body.id)
    .getOrNull<ServerTracker>()
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