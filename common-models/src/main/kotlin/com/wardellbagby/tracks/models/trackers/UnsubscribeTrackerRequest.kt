package com.wardellbagby.tracks.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class UnsubscribeTrackerRequest(
  val id: String
)
