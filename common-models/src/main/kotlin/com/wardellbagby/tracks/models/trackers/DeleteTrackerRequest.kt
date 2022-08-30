package com.wardellbagby.tracks.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTrackerRequest(
  val id: String
)
