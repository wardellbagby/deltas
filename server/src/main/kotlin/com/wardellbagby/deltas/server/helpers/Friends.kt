package com.wardellbagby.deltas.server.helpers

import com.wardellbagby.deltas.models.friends.FriendDTO
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.getOrEmpty
import com.wardellbagby.deltas.server.firebase.label
import com.wardellbagby.deltas.server.model.IdReference

suspend fun validateUIDsAreFriends(
  selfUID: String,
  ids: List<String>
): Boolean {
  if (ids.isEmpty()) {
    return true
  }

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
