package com.wardellbagby.tracks.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTrackerRequest(
  val id: String,
  val shouldResetTime: Boolean = false,
  val shouldIncrementCount: Boolean = false,
  val idsToShareWith: List<String>? = null,
  val label: String? = null
)
