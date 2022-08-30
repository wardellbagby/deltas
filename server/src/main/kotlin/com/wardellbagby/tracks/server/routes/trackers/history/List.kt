package com.wardellbagby.tracks.server.routes.trackers.history

import com.google.cloud.firestore.Query.Direction.DESCENDING
import com.google.firebase.auth.UserRecord
import com.wardellbagby.tracks.models.trackers.history.HistoryDTO
import com.wardellbagby.tracks.models.trackers.history.ListHistoryRequest
import com.wardellbagby.tracks.models.trackers.history.ListHistoryResponse
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrEmpty
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.helpers.failIfNull
import com.wardellbagby.tracks.server.model.ServerHistory
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

suspend fun UserRecord.canSeeTracker(trackerId: String): Boolean {
  database.collection("trackers")
    .document(trackerId)
    .getOrNull<ServerTracker>()
    .failIfNull()
    .mapCatching { (_, tracker) ->
      tracker.creator == uid || uid in (tracker.visibleTo ?: emptyList())
    }
    .fold(
      onSuccess = {
        return it
      },
      onFailure = {
        return false
      }
    )
}

fun Route.listHistory() = post("/list") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<ListHistoryRequest>() ?: return@post

  if (!user.canSeeTracker(body.trackerId)) {
    call.respond(
      ListHistoryResponse(
        history = emptyList(),
        cursor = null,
        success = false
      )
    )
    return@post
  }

  val cursorSnapshot = body.cursor?.let {
    database.collection("trackers")
      .document(body.trackerId)
      .collection("history")
      .document(it)
      .get()
      .awaitCatching()
      .getOrNull()
  }

  database.collection("trackers")
    .document(body.trackerId)
    .collection("history")
    .orderBy("time", DESCENDING)
    .let {
      if (cursorSnapshot != null) {
        it.startAfter(cursorSnapshot)
      } else {
        it
      }
    }
    .limit(minOf(20, body.limit ?: 20))
    .getOrEmpty<ServerHistory>()
    .fold(
      onSuccess = { histories ->
        call.respond(
          ListHistoryResponse(
            history = histories
              .toList()
              .map { (id, history) ->
                HistoryDTO(
                  id = id,
                  time = history.time,
                  label = history.label,
                  oldCount = history.oldCount,
                  newCount = history.newCount,
                  oldResetTime = history.oldResetTime
                )
              }
              .sortedByDescending { it.time },
            cursor = histories.lastOrNull()?.id
          )
        )
        finish()
        return@post
      },
      onFailure = {
        call.respond(
          ListHistoryResponse(
            history = emptyList(),
            cursor = null,
            success = false
          )
        )
        return@post
      }
    )
}
