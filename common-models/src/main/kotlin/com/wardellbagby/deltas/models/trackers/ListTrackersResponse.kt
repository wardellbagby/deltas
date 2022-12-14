package com.wardellbagby.deltas.models.trackers

import com.wardellbagby.deltas.models.ServerResponse
import com.wardellbagby.deltas.models.friends.FriendDTO
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class OwnerDTO(
  val label: String,
  val isSelf: Boolean
)

@Serializable
data class TrackerDTO(
  val id: String,
  val label: String,
  val type: TrackerType,
  val resetTime: Instant? = null,
  val count: Int? = null,
  val owner: OwnerDTO,
  val visibleTo: List<FriendDTO>,
  val visibility: TrackerVisibility,
  val isSubscribed: Boolean
)

@Serializable
data class ListTrackersResponse(
  val trackers: List<TrackerDTO> = emptyList(),
  val cursor: String? = null,
  override val success: Boolean = true,
  override val errorDetailMessage: String? = null
) : ServerResponse
