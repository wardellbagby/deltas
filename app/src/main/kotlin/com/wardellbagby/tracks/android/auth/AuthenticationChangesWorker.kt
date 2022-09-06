package com.wardellbagby.tracks.android.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.squareup.workflow1.Worker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
      .map { it != null }
      .catch { emit(false) }
  }
}