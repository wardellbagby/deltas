package com.wardellbagby.tracks.server.routes.trackers

import com.wardellbagby.tracks.models.trackers.GetTrackerRequest
import com.wardellbagby.tracks.models.trackers.GetTrackerResponse
import com.wardellbagby.tracks.models.trackers.TrackerVisibility.Public
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.helpers.failIfNull
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.model.toDTO
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
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

  call.respond(GetTrackerResponse(tracker = tracker.toDTO(id, user.uid)))
}