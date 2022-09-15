package com.wardellbagby.deltas.android.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.workflow1.Worker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthenticationChangesWorker
@Inject constructor() : Worker<Boolean> {
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
          // Fires off a request to make a new token, but will be handled by the Firebase service
          FirebaseMessaging.getInstance().token
        }
      }
      .map { it != null }
      .catch { emit(false) }
  }
}