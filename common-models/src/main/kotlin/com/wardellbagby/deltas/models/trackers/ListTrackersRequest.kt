package com.wardellbagby.deltas.models.trackers

import kotlinx.serialization.Serializable

@Serializable
data class ListTrackersRequest(
  val cursor: String?,
  val limit: Int? = null
)
