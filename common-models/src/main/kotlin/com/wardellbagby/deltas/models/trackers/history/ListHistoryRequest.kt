package com.wardellbagby.deltas.models.trackers.history

import com.wardellbagby.deltas.models.ServerResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ListHistoryRequest(
  val trackerId: String,
  val cursor: String?,
  val limit: Int? = null
)

@Serializable
data class HistoryDTO(
  val id: String,
  val time: Instant,
  val label: String? = null,
  val oldCount: Int? = null,
  val newCount: Int? = null,
  val oldResetTime: Instant? = null
)

@Serializable
data class ListHistoryResponse(
  val history: List<HistoryDTO>,
  val cursor: String?,
  override val success: Boolean = true,
  override val errorDetailMessage: String? = null
) : ServerResponse
