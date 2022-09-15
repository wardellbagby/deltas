package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.CreateTrackerRequest
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.push
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.model.ElapsedTracker
import com.wardellbagby.deltas.server.model.IncrementalTracker
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.server.routes.users.updateUserData
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject

fun Route.createTracker() = post("/create") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<CreateTrackerRequest>() ?: return@post

  val userDataRepository: UserDataRepository by this@createTracker.inject()

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
    .flatMap {
      userDataRepository.updateUserData(user) {
        copy(createdTrackers = createdTrackers.orEmpty() + it.id)
      }
    }
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
