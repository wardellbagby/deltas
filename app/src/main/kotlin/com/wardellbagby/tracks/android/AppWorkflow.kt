package com.wardellbagby.tracks.android

import android.content.Intent
import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.container.BodyAndOverlaysScreen
import com.squareup.workflow1.ui.container.Overlay
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.AppWorkflow.State
import com.wardellbagby.tracks.android.AppWorkflow.State.InitialLoad
import com.wardellbagby.tracks.android.AppWorkflow.State.LoggedIn
import com.wardellbagby.tracks.android.AppWorkflow.State.LoggedOut
import com.wardellbagby.tracks.android.auth.AuthWorkflow
import com.wardellbagby.tracks.android.auth.AuthenticationChangesWorker
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler.DeepLinkResult
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

typealias AppRendering = BodyAndOverlaysScreen<Screen, Overlay>

/**
 * The root Workflow that this app runs.
 *
 * The state that this Workflow uses can be thought of as the app's state. All of your major screen
 * transitions should happen here. For instance, if your app would have a settings screen, there
 * should likely be a new `ViewingSettings` state added here that will run a new `SettingsWorkflow`
 * that you'd handle.
 */
class AppWorkflow
@Inject constructor(
  private val authenticationChangesWorker: AuthenticationChangesWorker,
  private val authWorkflow: AuthWorkflow,
  private val loggedInWorkflow: LoggedInWorkflow,
  private val deepLinkHandler: DeepLinkHandler,
) : StatefulWorkflow<Intent?, State, Nothing, AppRendering>() {

  sealed interface State : Parcelable {
    @Parcelize
    object InitialLoad : State

    @Parcelize
    object LoggedOut : State

    @Parcelize
    data class LoggedIn(val deepLinkResult: DeepLinkResult) : State
  }

  override fun initialState(
    props: Intent?,
    snapshot: Snapshot?
  ): State = snapshot?.toParcelable() ?: InitialLoad

  override fun render(
    renderProps: Intent?,
    renderState: State,
    context: RenderContext
  ): AppRendering {
    context.runningWorker(authenticationChangesWorker) { isSignedIn ->
      if (isSignedIn) {
        action {
          state = LoggedIn(
            deepLinkResult = deepLinkHandler.handle(renderProps)
          )
        }
      } else {
        action { state = LoggedOut }
      }
    }

    return when (renderState) {
      is InitialLoad -> AppRendering(LoadingScreen)
      is LoggedIn -> context.renderChild(
        child = loggedInWorkflow,
        props = renderState.deepLinkResult
      )
        .let {
          AppRendering(
            body = it.screen,
            overlays = listOfNotNull(it.overlay)
          )
        }

      is LoggedOut -> AppRendering(context.renderChild(authWorkflow))
    }
  }

  override fun onPropsChanged(old: Intent?, new: Intent?, state: State): State {
    return when (state) {
      InitialLoad -> InitialLoad
      LoggedOut -> LoggedOut
      is LoggedIn -> LoggedIn(deepLinkResult = deepLinkHandler.handle(new))
    }
  }

  override fun snapshotState(state: State): Snapshot {
    return state.toSnapshot()
  }
}
