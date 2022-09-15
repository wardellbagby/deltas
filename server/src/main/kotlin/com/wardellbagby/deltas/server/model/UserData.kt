package com.wardellbagby.deltas.server.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
  val messageToken: String?,
  val createdTrackers: List<String>?,
  val followedTrackers: List<String>?
)
