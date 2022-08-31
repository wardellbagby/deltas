package com.wardellbagby.tracks.android.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.workflow1.Worker
import com.wardellbagby.tracks.android.firebase.notifications.NotificationService
import com.wardellbagby.tracks.models.RegisterNotificationTokenRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthenticationChangesWorker
@Inject constructor(
  private val service: NotificationService
) : Worker<Boolean> {
  override fun run(): Flow<Boolean> {
    return callbackFlow {
      val listener = AuthStateListener {
        trySend(it.currentUser)
      }
      FirebaseAuth.getInstance().addAuthStateListener(listener)
      awaitClose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }
      .onEach {
        if (it != null) {
          service.registerNotificationToken(
            RegisterNotificationTokenRequest(
              token = FirebaseMessaging.getInstance().token.await()
            )
          )
        }
      }
      .map { it != null }
      .catch { emit(false) }
  }
}