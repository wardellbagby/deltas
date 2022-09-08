package com.wardellbagby.deltas.server.model

import com.wardellbagby.deltas.models.trackers.OwnerDTO
import com.wardellbagby.deltas.models.trackers.TrackerDTO
import com.wardellbagby.deltas.models.trackers.TrackerType
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.models.trackers.TrackerVisibility
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.firebase.label
import com.wardellbagby.deltas.server.helpers.getUserAsFriend
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ServerTracker(
  val label: String,
  val type: TrackerType,
  val creator: String,
  val timestamp: Instant,
  val visibility: TrackerVisibility,
  val visibleTo: List<String>? = null,
  val resetTime: Instant? = null,
  val count: Int? = null,
)

fun ElapsedTracker(
  label: String,
  creator: String,
  visibleTo: List<String>?,
  visibility: TrackerVisibility,
  resetTime: Instant
) = ServerTracker(
  type = Elapsed,
  label = label,
  creator = creator,
  timestamp = resetTime,
  visibleTo = visibleTo,
  visibility = visibility,
  resetTime = resetTime
)

fun IncrementalTracker(
  label: String,
  creator: String,
  visibleTo: List<String>?,
  timestamp: Instant,
  visibility: TrackerVisibility,
  count: Int
) = ServerTracker(
  type = Incremental,
  label = label,
  creator = creator,
  timestamp = timestamp,
  visibleTo = visibleTo,
  visibility = visibility,
  count = count
)

fun ServerTracker.toDTO(
  id: String,
  selfUID: String,
  isUserSubscribed: Boolean
): TrackerDTO {
  val owner = auth.getUser(creator)
  val baseTracker = TrackerDTO(
    id = id,
    label = label,
    visibleTo = visibleTo
      ?.mapNotNull { getUserAsFriend(it) }
      ?: emptyList(),
    type = type,
    owner = OwnerDTO(
      label = owner.label,
      isSelf = owner.uid == selfUID
    ),
    visibility = visibility,
    isSubscribed = isUserSubscribed
  )

  return when (type) {
    Incremental -> baseTracker.copy(count = count)
    Elapsed -> baseTracker.copy(resetTime = resetTime)
  }
}
