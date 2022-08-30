package com.wardellbagby.tracks.android.loggedin

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.trackers.TrackersWorkflow
import com.wardellbagby.tracks.android.friends.FriendsWorkflow
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Trackers
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Friends
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Settings
import com.wardellbagby.tracks.android.settings.SettingsWorkflow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class LoggedInWorkflow
@Inject constructor(
  private val trackersWorkflow: TrackersWorkflow,
  private val friendsWorkflow: FriendsWorkflow,
  private val settingsWorkflow: SettingsWorkflow
) : StatefulWorkflow<Unit, State, Nothing, LoggedInRendering>() {

  sealed interface State : Parcelable {
    @Parcelize
    object Trackers : State

    @Parcelize
    object Friends : State

    @Parcelize
    object Settings : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: Trackers
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): LoggedInRendering {
    return when (renderState) {
      Trackers -> {
        val rendering = context.renderChild(trackersWorkflow)
        LoggedInRendering(
          screen = rendering.screen.wrapWithBottomNav(renderState, context),
          overlay = rendering.overlay
        )
      }

      Friends -> {
        val rendering = context.renderChild(friendsWorkflow)
        LoggedInRendering(
          screen = rendering.screen.wrapWithBottomNav(renderState, context),
          overlay = rendering.overlay
        )
      }

      Settings -> LoggedInRendering(
        screen = context.renderChild(settingsWorkflow)
          .wrapWithBottomNav(renderState, context)
      )
    }
  }

  override fun snapshotState(state: State): Snapshot {
    return state.toSnapshot()
  }

  private fun Screen.wrapWithBottomNav(
    renderState: State,
    context: RenderContext
  ): BottomNavigationRendering {
    return BottomNavigationRendering(
      wrapped = this,
      currentDestination = renderState.asDestination(),
      onDestinationChanged = context.eventHandler { destination: Destination ->
        state = when (destination) {
          Destination.Trackers -> Trackers
          Destination.Friends -> Friends
          Destination.Settings -> Settings
        }
      }
    )
  }

  private fun State.asDestination(): Destination {
    return when (this) {
      Trackers -> Destination.Trackers
      Friends -> Destination.Friends
      Settings -> Destination.Settings
    }
  }
}