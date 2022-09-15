package com.wardellbagby.deltas.server.helpers

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.firebase.messaging
import com.wardellbagby.deltas.server.model.ServerTracker
import com.wardellbagby.deltas.server.routes.trackers.TrackersRepository

private fun ServerTracker.toMulticastMessage(
  trackerId: String,
  tokens: List<String>
): MulticastMessage {
  val trackerAction = when (type) {
    Elapsed -> "has been reset!"
    Incremental -> "has been increased!"
  }
  return MulticastMessage.builder()
    .addAllTokens(tokens)
    .setNotification(
      Notification.builder()
        .setTitle("Tracker updated!")
        .setBody("The tracker \"${label}\" $trackerAction")
        .build()
    )
    .setAndroidConfig(
      AndroidConfig.builder()
        .setCollapseKey("tracker-updated")
        .setNotification(
          AndroidNotification.builder()
            .setTag(trackerId)
            .build()
        )
        .build()
    )
    .build()
}

suspend fun sendTrackerUpdateNotifications(
  trackersRepository: TrackersRepository,
  trackerId: String
): Result<Unit> {
  val tracker = trackersRepository.getTracker(trackerId)
    .failIfNull()
    .onFailure {
      return Result.failure(it)
    }
    .getOrThrow()
    .value

  return database.collection("users")
    .whereArrayContains("followedTrackers", trackerId)
    .get()
    .awaitCatching()
    .map { it.documents }
    .map { docs -> docs.mapNotNull { it.getString("messageToken") } }
    .flatMap { messageTokens ->
      runCatching {
        if (messageTokens.isNotEmpty()) {
          messaging.sendMulticast(tracker.toMulticastMessage(trackerId, messageTokens))
        }
      }
    }
}
