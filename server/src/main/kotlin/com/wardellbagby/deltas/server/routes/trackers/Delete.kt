package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.DeleteTrackerRequest
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.deleteTracker() = post("/delete") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<DeleteTrackerRequest>() ?: return@post

  val repository: TrackersRepository by this@deleteTracker.inject()

  val tracker = repository.getTracker(body.id)
    .failIfNull()
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

  repository.deleteTracker(body.id)
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
