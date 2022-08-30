package com.wardellbagby.tracks.server.helpers

import com.wardellbagby.tracks.models.friends.FriendDTO
import com.wardellbagby.tracks.server.firebase.auth
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrEmpty
import com.wardellbagby.tracks.server.firebase.label
import com.wardellbagby.tracks.server.model.IdReference

suspend fun validateUIDsAreFriends(
  selfUID: String,
  ids: List<String>
): Boolean {
  database.collection("users")
    .document(selfUID)
    .collection("friends")
    .whereIn("id", ids)
    .getOrEmpty<IdReference>()
    .fold(
      onSuccess = {
        return ids.distinct() == it.map { ref -> ref.value.id }.distinct()
      },
      onFailure = { return false }
    )
}

fun getUserAsFriend(id: String): FriendDTO? {
  val friend = auth.getUser(id) ?: return null
  return FriendDTO(id = id, label = friend.label)
}
