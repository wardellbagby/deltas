package com.wardellbagby.tracks.server.helpers

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.firebase.messaging
import com.wardellbagby.tracks.server.model.ServerTracker

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

suspend fun sendTrackerUpdateNotifications(trackerId: String): Result<Unit> {
  val trackerRef = database.collection("trackers").document(trackerId)
  val tracker = trackerRef
    .getOrNull<ServerTracker>()
    .failIfNull()
    .onFailure {
      return Result.failure(it)
    }
    .getOrThrow()
    .value

  return database.collection("users")
    .whereArrayContains("followedTrackers", trackerRef)
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
