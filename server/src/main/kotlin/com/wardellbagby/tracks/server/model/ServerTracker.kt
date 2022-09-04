package com.wardellbagby.tracks.server.model

import com.wardellbagby.tracks.models.trackers.TrackerType
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ServerTracker(
  val label: String,
  val type: TrackerType,
  val creator: String,
  val timestamp: Instant,
  val visibleTo: List<String>? = null,
  val resetTime: Instant? = null,
  val count: Int? = null,
)

fun ElapsedTracker(
  label: String,
  creator: String,
  visibleTo: List<String>?,
  resetTime: Instant
) = ServerTracker(
  type = Elapsed,
  label = label,
  creator = creator,
  timestamp = resetTime,
  visibleTo = visibleTo,
  resetTime = resetTime
)

fun IncrementalTracker(
  label: String,
  creator: String,
  visibleTo: List<String>?,
  timestamp: Instant,
  count: Int
) = ServerTracker(
  type = Incremental,
  label = label,
  creator = creator,
  timestamp = timestamp,
  visibleTo = visibleTo,
  count = count
)
