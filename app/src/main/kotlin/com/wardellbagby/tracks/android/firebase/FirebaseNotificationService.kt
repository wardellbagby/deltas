package com.wardellbagby.tracks.android.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.wardellbagby.tracks.models.RegisterNotificationTokenRequest
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
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

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}