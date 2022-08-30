package com.wardellbagby.tracks.server.routes.friends

import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.friends.AddFriendRequest
import com.wardellbagby.tracks.server.firebase.auth
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.push
import com.wardellbagby.tracks.server.model.IdReference
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.addFriend() = post("/add") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<AddFriendRequest>() ?: return@post

  val newFriend = runCatching { auth.getUserByEmail(body.email) }
    .fold(
      onSuccess = { it },
      onFailure = {
        call.respond(
          DefaultServerResponse(
            success = false,
            "Unable to find friend with the provided email"
          )
        )
        return@post
      }
    )

  database.collection("users")
    .document(user.uid)
    .collection("friends")
    .push(IdReference(newFriend.uid))
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = {
        call.respond(
          DefaultServerResponse(
            success = false,
            errorDetailMessage = "Failed to add friend"
          )
        )
      }
    )
}
