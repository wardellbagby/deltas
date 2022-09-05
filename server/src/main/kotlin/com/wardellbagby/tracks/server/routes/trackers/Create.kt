package com.wardellbagby.tracks.server.routes.trackers

import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.trackers.CreateTrackerRequest
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.push
import com.wardellbagby.tracks.server.model.ElapsedTracker
import com.wardellbagby.tracks.server.model.IncrementalTracker
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock

fun Route.createTracker() = post("/create") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<CreateTrackerRequest>() ?: return@post

  val tracker = when (body.type) {
    Elapsed -> ElapsedTracker(
      resetTime = Clock.System.now(),
      label = body.label,
      creator = user.uid,
      visibility = body.visibility,
      visibleTo = emptyList()
    )

    Incremental -> IncrementalTracker(
      count = 0,
      label = body.label,
      creator = user.uid,
      visibility = body.visibility,
      visibleTo = emptyList(),
      timestamp = Clock.System.now()
    )
  }

  database.collection("trackers")
    .push(tracker)
    .fold(
      onSuccess = {
        call.respond(DefaultServerResponse())
      },
      onFailure = {
        call.respond(
          DefaultServerResponse(
            success = false,
            errorDetailMessage = "Failed to create tracker"
          )
        )
        return@post
      }
    )
}
