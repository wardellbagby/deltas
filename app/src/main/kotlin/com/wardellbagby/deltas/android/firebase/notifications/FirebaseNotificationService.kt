package com.wardellbagby.deltas.android.firebase.notifications

import android.content.Intent
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
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseNotificationService : FirebaseMessagingService() {
  private val scope = CoroutineScope(Job() + Dispatchers.IO)

  @Inject
  lateinit var notificationService: NotificationService

  @Inject
  lateinit var remoteTrackerChangesRepository: RemoteTrackerChangesRepository

  override fun getStartCommandIntent(originalIntent: Intent): Intent {
    if (originalIntent.isSupported()) {
      return originalIntent
    }
    return super.getStartCommandIntent(originalIntent)
  }

  override fun handleIntent(intent: Intent) {
    when (intent.action) {
      AUTHENTICATION_CHANGED_ACTION -> onAuthenticationChanged()
      else -> super.handleIntent(intent)
    }

    // FirebaseMessagingService uses executors (which are backed by threads) to launch everything,
    // and it expects anything handling an intent to just block the thread until it's done. This
    // lets us do that thread blocking (so that the service isn't destroyed prematurely because
    // FirebaseMessagingService thinks we're done working) while still using coroutines (mostly) as
    // normal. This also still allows the OS to kill the service while letting the coroutines
    // gracefully cancel.
    runBlocking {
      scope.coroutineContext.job.join()
    }
  }

  private fun onAuthenticationChanged() {
    FirebaseAuth.getInstance().currentUser ?: return

    scope.launch {
      val token = FirebaseMessaging.getInstance().token.await()
      onNewToken(token)
    }
  }

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

  private fun Intent.isSupported(): Boolean {
    return action == AUTHENTICATION_CHANGED_ACTION
  }

  companion object {
    const val AUTHENTICATION_CHANGED_ACTION = "${BuildConfig.APPLICATION_ID}.intent.AUTH_CHANGED"
  }
}