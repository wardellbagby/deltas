package com.wardellbagby.deltas.server.routes.friends

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.friends.AddFriendRequest
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.push
import com.wardellbagby.deltas.server.model.IdReference
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
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
