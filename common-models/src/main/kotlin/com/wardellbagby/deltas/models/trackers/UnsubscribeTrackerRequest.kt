package com.wardellbagby.deltas.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class UnsubscribeTrackerRequest(
  val id: String
)

@Serializable
data class SubscribeTrackerRequest(
  val id: String
)
