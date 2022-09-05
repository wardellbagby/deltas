package com.wardellbagby.tracks.android.deeplinks

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.utils.nullIfEmpty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class DeepLinkHandler
@Inject constructor(
  @ApplicationContext context: Context
) {
  private val resources = context.resources

  sealed interface DeepLinkResult : Parcelable {
    @Parcelize
    object None : DeepLinkResult

    @Parcelize
    data class ViewTracker(val id: String) : DeepLinkResult
  }

  fun handle(intent: Intent?): DeepLinkResult {
    if (intent == null ||
      intent.action != Intent.ACTION_VIEW ||
      intent.scheme != resources.getString(R.string.deep_link_scheme)
    ) {
      return DeepLinkResult.None
    }

    val data = intent.data ?: return DeepLinkResult.None

    if (data.host != resources.getString(R.string.deep_link_host)) {
      return DeepLinkResult.None
    }

    val path = data.path
      ?.split("/")
      ?.nullIfEmpty()
      ?.filter { it.isNotNullOrBlank() }
      ?: return DeepLinkResult.None
    return when (path.first()) {
      "view" -> {
        val id = path.getOrNull(1) ?: return DeepLinkResult.None
        DeepLinkResult.ViewTracker(id)
      }

      else -> DeepLinkResult.None
    }
  }
}