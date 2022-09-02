package com.wardellbagby.tracks.android.loggedin

import android.os.Parcelable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@Parcelize
data class TrackerChanged(
  val message: String
) : Parcelable

@Singleton
class RemoteTrackerChangesRepository
@Inject constructor() {
  private val changes = MutableSharedFlow<TrackerChanged>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val trackerChanges: Flow<TrackerChanged> = changes

  fun onTrackerChangedRemotely(message: String) {
    changes.tryEmit(TrackerChanged(message))
  }
}