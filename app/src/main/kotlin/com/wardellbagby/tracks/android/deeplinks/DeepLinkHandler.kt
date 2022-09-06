package com.wardellbagby.tracks.android.deeplinks

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler.DeepLinkResult.None
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler.DeepLinkResult.ViewTracker
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.utils.nullIfEmpty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkHandler
@Inject constructor(
  @ApplicationContext context: Context
) {
  private val deepLink = MutableStateFlow<DeepLinkResult>(None)
  private val resources = context.resources

  val currentDeepLink: StateFlow<DeepLinkResult> = deepLink

  sealed interface DeepLinkResult : Parcelable {
    @Parcelize
    object None : DeepLinkResult

    @Parcelize
    data class ViewTracker(val id: String) : DeepLinkResult
  }

  fun onDeepLinkHandled() {
    deepLink.value = None
  }

  fun onNewIntent(intent: Intent?) {
    deepLink.value = intent.asDeepLinkResult()
  }

  private fun Intent?.asDeepLinkResult(): DeepLinkResult {
    if (this == null ||
      action != Intent.ACTION_VIEW ||
      scheme != resources.getString(R.string.deep_link_scheme)
    ) {
      return None
    }

    val data = data ?: return None

    if (data.host != resources.getString(R.string.deep_link_host)) {
      return None
    }

    val path = data.path
      ?.split("/")
      ?.nullIfEmpty()
      ?.filter { it.isNotNullOrBlank() }
      ?: return None
    return when (path.first()) {
      "view" -> {
        val id = path.getOrNull(1) ?: return None
        ViewTracker(id)
      }

      else -> None
    }
  }
}