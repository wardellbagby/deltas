package com.wardellbagby.tracks.models.trackers

import com.wardellbagby.tracks.models.ServerResponse
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
