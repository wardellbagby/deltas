package com.wardellbagby.deltas.android.auth

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.squareup.workflow1.Worker
import com.wardellbagby.deltas.android.firebase.notifications.FirebaseNotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthenticationChangesWorker
@Inject constructor(
  @ApplicationContext private val context: Context
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
          context.startService(
            Intent(context, FirebaseNotificationService::class.java)
              .apply {
                action = FirebaseNotificationService.AUTHENTICATION_CHANGED_ACTION
              })
        }
      }
      .map { it != null }
      .catch { emit(false) }
  }
}