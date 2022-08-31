package com.wardellbagby.tracks.android.firebase.notifications

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.models.RegisterNotificationTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseNotificationService : FirebaseMessagingService() {
  private val scope = CoroutineScope(Job() + Dispatchers.IO)

  @Inject
  lateinit var notificationService: NotificationService

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    scope.launch {
      if (token.isNotNullOrBlank()) {
        notificationService.registerNotificationToken(
          request = RegisterNotificationTokenRequest(token = token)
        )
      }
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)

    val displayableMessage = message.notification?.body ?: return

    sendBroadcast(
      Intent(TRACKER_CHANGED_ACTION)
        .putExtra(TRACKER_CHANGED_MESSAGE, displayableMessage),
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}