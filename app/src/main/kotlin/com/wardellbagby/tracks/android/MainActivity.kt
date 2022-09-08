package com.wardellbagby.tracks.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.renderWorkflowIn
import com.wardellbagby.tracks.android.core_ui.getActual
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler
import com.wardellbagby.tracks.android.networking.Endpoint
import com.wardellbagby.tracks.android.theming.AppCompositionRoot
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The single Activity of this application. Its job is to host the single View Model of this app
 * that will host the singular root Workflow. If you're thinking of adding a new Activity, you
 * probably shouldn't. Instead, make changes to [AppWorkflow] to have a new state that renders a
 * new child Workflow instead.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  @Inject
  lateinit var activityProvider: ActivityProvider

  @Inject
  lateinit var endpoint: Endpoint

  @Inject
  lateinit var deepLinkHandler: DeepLinkHandler

  private val model: AppViewModel by viewModels()
  private val inputMethodManager by lazy {
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

  @OptIn(ExperimentalComposeUiApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Allow [activityProvider] to get an instance to this activity. If other classes need similar
    // functionality, refactor this to instead use Dagger's "IntoSet" to provide all classes that
    // need to observe the activity's lifecycle.
    lifecycle.addObserver(activityProvider)

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
        endpoint.changes
          .collectLatest {
            finish()
            startActivity(intent)
          }
      }
    }

    deepLinkHandler.onNewIntent(intent)

    setContent {
      val rendering by model.renderings.collectAsState()
      val actualRendering = rendering.getActual()

      val focusManager = LocalFocusManager.current
      val keyboardController = LocalSoftwareKeyboardController.current
      val view = LocalView.current

      LaunchedEffect(actualRendering.javaClass) {
        // Really try super hard to hide the soft keyboard.
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        inputMethodManager.hideSoftInputFromWindow(
          view.windowToken,
          0
        )

      }

      // TODO File another Workflow issue about withCompositionRoot not working
      AppCompositionRoot {
        WorkflowRendering(
          rendering = rendering,
          viewEnvironment = ViewEnvironment.EMPTY
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    deepLinkHandler.onNewIntent(intent)
  }

  override fun onResume() {
    super.onResume()
    if (SDK_INT >= TIRAMISU && checkSelfPermission(POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
      requestPermissions(arrayOf(POST_NOTIFICATIONS), 0)
    }
  }
}

/**
 * The View Model that stores and runs the root Workflow. With how Workflows work, View Models
 * as a concept aren't needed as Workflows are effectively View Models themselves. In that Workflows
 * allow you to have a single source of state and to create a model from that state that can be used
 * by your view layer in order to render UI.
 *
 * However, we need a single View Model at the root level here in order to keep our Workflows
 * outside of the normal Android lifecycle. Therefore, we use a View Model to host the root
 * Workflow and handle its renderings.
 *
 * Much of this is machinery that you only need a surface-level understanding of; it's unlikely this
 * will ever need any changes, as all new features should be Workflows themselves, and those
 * Workflows should be children of [AppWorkflow]. Due to that, those will all use the Workflow
 * machinery instead of View Models.
 */
@HiltViewModel
class AppViewModel
@Inject constructor(
  savedState: SavedStateHandle,
  workflow: AppWorkflow
) : ViewModel() {
  val renderings: StateFlow<Screen> =
    renderWorkflowIn(
      workflow = workflow,
      scope = viewModelScope,
      savedStateHandle = savedState,
      interceptors = listOf(DebugWorkflowLoggingInterceptor)
    )
}
