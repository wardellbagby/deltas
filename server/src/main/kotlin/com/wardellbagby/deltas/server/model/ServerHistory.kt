package com.wardellbagby.deltas.server.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ServerHistory(
  val time: Instant,
  val label: String? = null,
  val newCount: Int? = null,
  val oldCount: Int? = null,
  val oldResetTime: Instant? = null
)

fun ElapsedTrackerHistory(
  time: Instant,
  label: String?,
  oldResetTime: Instant
) = ServerHistory(
  time = time,
  label = label,
  oldResetTime = oldResetTime
)

fun IncrementalTrackerHistory(
  time: Instant,
  label: String?,
  newCount: Int,
  oldCount: Int
) = ServerHistory(
  time = time,
  label = label,
  oldCount = oldCount,
  newCount = newCount,
)
