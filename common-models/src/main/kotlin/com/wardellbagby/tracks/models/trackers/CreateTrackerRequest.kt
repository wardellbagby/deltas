package com.wardellbagby.tracks.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class CreateTrackerRequest(
  val type: TrackerType,
  val label: String,
  val visibility: TrackerVisibility
)
