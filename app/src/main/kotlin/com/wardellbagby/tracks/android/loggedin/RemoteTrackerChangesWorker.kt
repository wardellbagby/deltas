package com.wardellbagby.tracks.android.loggedin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Parcelable
import com.squareup.workflow1.Worker
import com.wardellbagby.tracks.android.firebase.notifications.TRACKER_CHANGED_ACTION
import com.wardellbagby.tracks.android.firebase.notifications.TRACKER_CHANGED_MESSAGE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class TrackerChanged(
  val message: String
) : Parcelable

class RemoteTrackerChangesWorker
@Inject constructor(
  @ApplicationContext private val context: Context
) : Worker<TrackerChanged> {
  override fun run(): Flow<TrackerChanged> {
    return callbackFlow {
      val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          if (intent.action == TRACKER_CHANGED_ACTION) {
            val message = intent.getStringExtra(TRACKER_CHANGED_MESSAGE) ?: return
            trySend(TrackerChanged(message = message))
          }
        }
      }
      context.registerReceiver(receiver, IntentFilter().apply { addAction(TRACKER_CHANGED_ACTION) })
      awaitClose { context.unregisterReceiver(receiver) }
    }
  }
}