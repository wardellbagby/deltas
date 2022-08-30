package com.wardellbagby.tracks.server.routes.friends

import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.friends.DeleteFriendRequest
import com.wardellbagby.tracks.server.firebase.await
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.deleteFriend() = post("/delete") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<DeleteFriendRequest>() ?: return@post

  val friendRef =
    database.collection("users")
      .document(user.uid)
      .collection("friends")
      .document(body.id)

  runCatching {
    friendRef.delete().await()
  }.fold(
    onSuccess = { call.respond(DefaultServerResponse()) },
    onFailure = { call.respond(DefaultServerResponse(success = false)) }
  )
}
