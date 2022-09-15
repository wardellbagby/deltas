package com.wardellbagby.deltas.android.firebase.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wardellbagby.deltas.android.loggedin.RemoteTrackerChangesRepository
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.android.strings.isNotNullOrBlank
import com.wardellbagby.deltas.models.RegisterNotificationTokenRequest
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
    if (FirebaseAuth.getInstance().currentUser == null) {
      FirebaseMessaging.getInstance().deleteToken()
      return
    }

    scope.launch {
      if (token.isNotNullOrBlank()) {
        Log.v(BuildConfig.APPLICATION_ID, "Registering new Firebase token")
        val result = notificationService.registerNotificationToken(
          request = RegisterNotificationTokenRequest(token = token)
        )
        if (result is NetworkResult.Failure) {
          Log.w(
            BuildConfig.APPLICATION_ID,
            "Failed to register new Firebase token: ${result.message}",
          )
          FirebaseMessaging.getInstance().deleteToken()
        }
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