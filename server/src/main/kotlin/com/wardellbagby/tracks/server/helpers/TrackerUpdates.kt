package com.wardellbagby.tracks.server.helpers

import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.push
import com.wardellbagby.tracks.server.logger
import com.wardellbagby.tracks.server.model.ElapsedTrackerHistory
import com.wardellbagby.tracks.server.model.IncrementalTrackerHistory
import com.wardellbagby.tracks.server.model.ServerTracker
import kotlinx.datetime.Clock

suspend fun pushTrackerHistoryUpdate(
  trackerId: String,
  oldTracker: ServerTracker,
  newTracker: ServerTracker,
  label: String?
): Result<Unit> = runCatching {
  when (newTracker.type) {
    Elapsed -> ElapsedTrackerHistory(
      time = newTracker.resetTime
        ?: error("No reset time"),
      label = label,
      oldResetTime = oldTracker.resetTime ?: error("No reset time")
    )

    Incremental -> IncrementalTrackerHistory(
      time = Clock.System.now(),
      label = label,
      oldCount = oldTracker.count ?: error("No tracker count"),
      newCount = newTracker.count ?: error("No tracker count")
    )
  }
}
  .flatMap {
    database.collection("trackers")
      .document(trackerId)
      .collection("history")
      .push(it)
  }
  .onFailure {
    logger.error("Failed to push tracker update", it)
  }
