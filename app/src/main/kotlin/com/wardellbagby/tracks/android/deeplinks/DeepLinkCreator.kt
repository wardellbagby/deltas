package com.wardellbagby.tracks.android.deeplinks

import android.content.Intent
import com.wardellbagby.tracks.android.BuildConfig
import com.wardellbagby.tracks.android.trackers.models.Tracker
import javax.inject.Inject

class DeepLinkCreator
@Inject constructor() {
  fun createShareTrackerIntent(tracker: Tracker): Intent {
    return Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, tracker.asViewUrl())
    }
  }

  private fun Tracker.asViewUrl(): String {
    val scheme = BuildConfig.DEEP_LINK_SCHEME
    val host = BuildConfig.DEEP_LINK_HOST

    return "$scheme://$host/view/$id"
  }
}