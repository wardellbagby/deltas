package com.wardellbagby.deltas.server.helpers

import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.push
import com.wardellbagby.deltas.server.logger
import com.wardellbagby.deltas.server.model.ElapsedTrackerHistory
import com.wardellbagby.deltas.server.model.IncrementalTrackerHistory
import com.wardellbagby.deltas.server.model.ServerTracker
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
      .map { }
  }
  .onFailure {
    logger.error("Failed to push tracker update", it)
  }
