package com.wardellbagby.deltas.server.routes.trackers

import com.google.cloud.firestore.FieldValue.arrayRemove
import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Private
import com.wardellbagby.deltas.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.helpers.removeFollowedTracker
import com.wardellbagby.deltas.server.model.ServerTracker
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.unsubscribeTracker() = post("/unsubscribe") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<UnsubscribeTrackerRequest>() ?: return@post

  val trackerRef = database.collection("trackers")
    .document(body.id)
  val tracker = trackerRef
    .getOrNull<ServerTracker>()
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

    trackerRef
      .update("visibleTo", arrayRemove(user.uid))
      .awaitCatching()
      .flatMap {
        database.collection("users")
          .document(user.uid)
          .set(mapOf("followedTrackers" to arrayRemove(body.id)))
          .awaitCatching()
      }
      .onFailure { call.respond(DefaultServerResponse(success = false)) }
  }

  user.removeFollowedTracker(trackerRef)
    .fold(
      onSuccess = { call.respond(DefaultServerResponse(success = true)) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )

}