package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.trackers.SubscribeTrackerRequest
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Public
import com.wardellbagby.deltas.server.helpers.failIfNull
import com.wardellbagby.deltas.server.helpers.flatMap
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.server.routes.users.updateUserData
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.subscribeTracker() = post("/subscribe") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<SubscribeTrackerRequest>() ?: return@post

  val trackersRepository: TrackersRepository by this@subscribeTracker.inject()
  val userDataRepository: UserDataRepository by this@subscribeTracker.inject()

  trackersRepository.getTracker(body.id)
    .failIfNull()
    .flatMap {
      if (it.value.visibility != Public) {
        Result.failure(Exception("Tracker is not public."))
      } else {
        Result.success(it)
      }
    }
    .onFailure {
      call.respond(DefaultServerResponse(success = false))
      return@post
    }
    .getOrThrow()

  userDataRepository.updateUserData(user) {
    copy(
      followedTrackers = followedTrackers.orEmpty() + body.id
    )
  }
    .fold(
      onSuccess = { call.respond(DefaultServerResponse(success = true)) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
