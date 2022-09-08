package com.wardellbagby.deltas.models.friends

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFriendRequest(
  val id: String
)
