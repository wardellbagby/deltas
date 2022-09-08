package com.wardellbagby.deltas.models.trackers

import com.wardellbagby.deltas.models.ServerResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetTrackerRequest(
  val id: String
)

@Serializable
data class GetTrackerResponse(
  val tracker: TrackerDTO?,
  override val success: Boolean = true,
  override val errorDetailMessage: String? = null
) : ServerResponse
