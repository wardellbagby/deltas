package com.wardellbagby.tracks.models.friends

import kotlinx.serialization.Serializable

@Serializable
data class ListFriendsRequest(
  val cursor: String?,
  val limit: Int? = null
)
