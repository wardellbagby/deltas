package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.SubscribeTrackerRequest
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.helpers.addFollowedTracker
import com.wardellbagby.deltas.server.helpers.failIfDoesNotExist
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.subscribeTracker() = post("/subscribe") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<SubscribeTrackerRequest>() ?: return@post

  val trackerRef = database.collection("trackers")
    .document(body.id)
    .get()
    .awaitCatching()
    .failIfDoesNotExist()
    .onFailure {
      call.respond(DefaultServerResponse(success = false))
      return@post
    }
    .getOrThrow()

  user.addFollowedTracker(trackerRef.reference)
    .fold(
      onSuccess = { call.respond(DefaultServerResponse(success = true)) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
