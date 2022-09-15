package com.wardellbagby.deltas.server.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
  val messageToken: String? = null,
  val createdTrackers: List<String>? = null,
  val followedTrackers: List<String>? = null
)
