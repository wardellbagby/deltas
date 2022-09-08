package com.wardellbagby.deltas.server.routes.friends

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.friends.DeleteFriendRequest
import com.wardellbagby.deltas.server.firebase.await
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
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
