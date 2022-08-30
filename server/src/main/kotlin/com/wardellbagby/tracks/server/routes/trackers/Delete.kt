package com.wardellbagby.tracks.server.routes.trackers

import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.trackers.DeleteTrackerRequest
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.deleteTracker() = post("/delete") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<DeleteTrackerRequest>() ?: return@post

  val trackerRef = database.collection("trackers").document(body.id)
  val tracker = trackerRef.getOrNull<ServerTracker>()
    .mapCatching { it!! }
    .fold(
      onSuccess = { it },
      onFailure = {
        call.respond(DefaultServerResponse(success = false))
        return@post
      }
    )
    .value

  if (tracker.creator != user.uid) {
    call.respond(DefaultServerResponse(success = false))
    return@post
  }

  trackerRef.delete()
    .awaitCatching()
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
