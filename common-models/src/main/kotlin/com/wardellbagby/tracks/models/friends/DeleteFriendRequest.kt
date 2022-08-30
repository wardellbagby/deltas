package com.wardellbagby.tracks.models.friends

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFriendRequest(
  val id: String
)
