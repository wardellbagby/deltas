package com.wardellbagby.tracks.android.trackers.models

import android.os.Parcelable
import com.wardellbagby.tracks.android.friends.Friend
import com.wardellbagby.tracks.android.friends.toModels
import com.wardellbagby.tracks.android.trackers.models.Tracker.ElapsedTimeTracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.IncrementalTracker
import com.wardellbagby.tracks.models.trackers.TrackerDTO
import com.wardellbagby.tracks.models.trackers.TrackerType
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import kotlinx.datetime.toJavaInstant
import kotlinx.parcelize.Parcelize
import java.time.Instant

sealed interface Tracker : Parcelable {
  val id: String
  val label: String
  val ownerLabel: String
  val canEdit: Boolean
  val visibleTo: List<Friend>

  @Parcelize
  data class ElapsedTimeTracker(
    override val id: String,
    override val label: String,
    override val ownerLabel: String,
    override val canEdit: Boolean,
    override val visibleTo: List<Friend>,
    // We use Java Instant here because it's serializable
    val resetTime: Instant
  ) : Tracker

  @Parcelize
  data class IncrementalTracker(
    override val id: String,
    override val label: String,
    override val ownerLabel: String,
    override val canEdit: Boolean,
    override val visibleTo: List<Friend>,
    val count: UInt
  ) : Tracker
}

val Tracker.type: TrackerType
  get() = when (this) {
    is ElapsedTimeTracker -> Elapsed
    is IncrementalTracker -> Incremental
  }

fun TrackerDTO.asModel(): Tracker {
  return when (type) {
    Elapsed -> ElapsedTimeTracker(
      id = id,
      label = label,
      resetTime = resetTime!!.toJavaInstant(),
      ownerLabel = owner.label,
      canEdit = owner.isSelf,
      visibleTo = visibleTo.toModels()
    )

    Incremental -> IncrementalTracker(
      id = id,
      label = label,
      count = count!!.toUInt(),
      ownerLabel = owner.label,
      canEdit = owner.isSelf,
      visibleTo = visibleTo.toModels()
    )
  }
}

fun List<TrackerDTO>.asModel(): List<Tracker> = map { it.asModel() }