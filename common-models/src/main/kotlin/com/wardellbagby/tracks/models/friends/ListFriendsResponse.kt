package com.wardellbagby.tracks.models.friends

import com.wardellbagby.tracks.models.ServerResponse
import kotlinx.serialization.Serializable

@Serializable
data class FriendDTO(
  val id: String,
  val label: String,
)

@Serializable
data class ListFriendsResponse(
  val friends: List<FriendDTO>,
  override val success: Boolean = true,
  override val errorDetailMessage: String? = null
) : ServerResponse
