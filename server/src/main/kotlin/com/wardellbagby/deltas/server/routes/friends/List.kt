package com.wardellbagby.deltas.server.routes.friends

import com.wardellbagby.deltas.models.friends.ListFriendsResponse
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrNull
import com.wardellbagby.deltas.server.helpers.combine
import com.wardellbagby.deltas.server.helpers.getUserAsFriend
import com.wardellbagby.deltas.server.logger
import com.wardellbagby.deltas.server.model.IdReference
import com.wardellbagby.deltas.server.routes.getUser
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.listFriends() = get("/list") {
  val user = call.getUser() ?: return@get

  // TODO paginate
  database.collection("users")
    .document(user.uid)
    .collection("friends")
    .listDocuments()
    .map { it.getOrNull<IdReference>() }
    .combine()
    .mapCatching {
      it
        .filterNotNull()
        .mapNotNull { ref -> getUserAsFriend(ref.value.id) }
    }
    .fold(
      onSuccess = {
        call.respond(ListFriendsResponse(friends = it))
      },
      onFailure = {
        logger.error("List Friends error", it)
        call.respond(
          ListFriendsResponse(
            success = false,
            friends = emptyList()
          )
        )
      }
    )
}
