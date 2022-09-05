package com.wardellbagby.tracks.server.routes.trackers

import com.google.cloud.firestore.FieldValue.arrayRemove
import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.helpers.failIfNull
import com.wardellbagby.tracks.server.helpers.flatMap
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.unsubscribeTracker() = post("/unsubscribe") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<UnsubscribeTrackerRequest>() ?: return@post

  val trackerRef = database.collection("trackers")
    .document(body.id)
  val trackerSubscribers = trackerRef.getOrNull<ServerTracker>()
    .failIfNull()
    .fold(
      onSuccess = { it.value.visibleTo ?: emptyList() },
      onFailure = {
        call.respond(DefaultServerResponse(success = false))
        return@post
      }
    )

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
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
