package com.wardellbagby.tracks.android.deeplinks

import android.content.Context
import android.content.Intent
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.trackers.models.Tracker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeepLinkCreator
@Inject constructor(
  @ApplicationContext context: Context
) {
  private val resources = context.resources
  fun createShareTrackerIntent(tracker: Tracker): Intent {
    return Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, tracker.asViewUrl())
    }
  }

  private fun Tracker.asViewUrl(): String {
    val scheme = resources.getString(R.string.deep_link_scheme)
    val host = resources.getString(R.string.deep_link_host)

    return "$scheme://$host/view/$id"
  }
}