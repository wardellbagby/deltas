package com.wardellbagby.deltas.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTrackerRequest(
  val id: String
)
