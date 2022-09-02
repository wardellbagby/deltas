package com.wardellbagby.tracks.android.firebase.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wardellbagby.tracks.android.loggedin.RemoteTrackerChangesRepository
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

  @Inject
  lateinit var remoteTrackerChangesRepository: RemoteTrackerChangesRepository

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

    remoteTrackerChangesRepository.onTrackerChangedRemotely(
      message = message.notification?.body ?: return
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}