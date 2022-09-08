package com.wardellbagby.deltas.android.deeplinks

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wardellbagby.deltas.android.BuildConfig
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler.DeepLinkResult.LoginAttempt
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler.DeepLinkResult.None
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler.DeepLinkResult.ViewTracker
import com.wardellbagby.deltas.android.strings.isNotNullOrBlank
import com.wardellbagby.deltas.utils.nullIfEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkHandler
@Inject constructor() {
  private val deepLink = MutableStateFlow<DeepLinkResult>(None)

  val currentDeepLink: StateFlow<DeepLinkResult> = deepLink

  sealed interface DeepLinkResult : Parcelable {
    @Parcelize
    object None : DeepLinkResult

    @Parcelize
    data class ViewTracker(val id: String) : DeepLinkResult

    @Parcelize
    data class LoginAttempt(val data: String) : DeepLinkResult
  }

  fun onDeepLinkHandled() {
    deepLink.value = None
  }

  fun onNewIntent(intent: Intent?) {
    deepLink.value = intent.asDeepLinkResult()
  }

  private fun Intent?.asDeepLinkResult(): DeepLinkResult {
    val data = this?.data ?: return None

    return when {
      Firebase.auth.isSignInWithEmailLink(data.toString()) -> {
        LoginAttempt(data.toString())
      }

      action == Intent.ACTION_VIEW && scheme == BuildConfig.DEEP_LINK_SCHEME -> {
        validateTrackerDeepLink(data)
      }

      else -> None
    }
  }

  private fun validateTrackerDeepLink(data: Uri): DeepLinkResult {
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