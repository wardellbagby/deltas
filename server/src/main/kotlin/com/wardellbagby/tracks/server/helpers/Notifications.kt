package com.wardellbagby.tracks.server.helpers

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.getOrNull
import com.wardellbagby.tracks.server.firebase.messaging
import com.wardellbagby.tracks.server.model.ServerTracker
import com.wardellbagby.tracks.server.model.User

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
  return database.collection("trackers").document(trackerId)
    .getOrNull<ServerTracker>()
    .failIfNull()
    .flatMap { (_, tracker) ->
      val tokens = (tracker.visibleTo ?: emptyList())
        .map { uid ->
          database.collection("users")
            .document(uid)
            .getOrNull<User>()
            .failIfNull()
            .mapCatching { (_, user) -> user.messageToken }
        }
        .combine()

      tokens.map { tracker to it }
    }
    .flatMap { (tracker, messageTokens) ->
      runCatching {
        if (messageTokens.isNotEmpty()) {
          messaging.sendMulticast(tracker.toMulticastMessage(trackerId, messageTokens))
        }
      }
    }
}
