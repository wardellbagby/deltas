package com.wardellbagby.tracks.android.loggedin

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler
import com.wardellbagby.tracks.android.deeplinks.DeepLinkHandler.DeepLinkResult.ViewTracker
import com.wardellbagby.tracks.android.friends.FriendsWorkflow
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Friends
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Settings
import com.wardellbagby.tracks.android.loggedin.LoggedInWorkflow.State.Trackers
import com.wardellbagby.tracks.android.settings.SettingsWorkflow
import com.wardellbagby.tracks.android.trackers.TrackersWorkflow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class LoggedInWorkflow
@Inject constructor(
  private val trackersWorkflow: TrackersWorkflow,
  private val friendsWorkflow: FriendsWorkflow,
  private val settingsWorkflow: SettingsWorkflow,
  private val remoteTrackerChangesRepository: RemoteTrackerChangesRepository,
  private val deepLinkHandler: DeepLinkHandler
) : StatefulWorkflow<Unit, State, Nothing, LoggedInRendering>() {

  sealed interface State : Parcelable {
    val trackerChanged: TrackerChanged?

    @Parcelize
    data class Trackers(
      override val trackerChanged: TrackerChanged? = null,
    ) : State

    @Parcelize
    data class Friends(
      override val trackerChanged: TrackerChanged? = null
    ) : State

    @Parcelize
    data class Settings(
      override val trackerChanged: TrackerChanged? = null
    ) : State

    fun with(trackerChanged: TrackerChanged?): State {
      return when (this) {
        is Friends -> copy(trackerChanged = trackerChanged)
        is Settings -> copy(trackerChanged = trackerChanged)
        is Trackers -> copy(trackerChanged = trackerChanged)
      }
    }
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: Trackers()
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): LoggedInRendering {
    context.runningWorker(
      deepLinkHandler.currentDeepLink
        .filterIsInstance<ViewTracker>()
        .asWorker()
    ) {
      action {
        state = Trackers()
      }
    }

    context.runningWorker(
      worker = remoteTrackerChangesRepository.trackerChanges.asWorker()
    ) {
      action {
        state = state.with(trackerChanged = it)
      }
    }
    return when (renderState) {
      is Trackers -> {
        val rendering = context.renderChild(
          child = trackersWorkflow
        )
        LoggedInRendering(
          screen = rendering.screen.wrapWithBottomNav(renderState, context),
          overlay = rendering.overlay
        )
      }

      is Friends -> {
        val rendering = context.renderChild(friendsWorkflow)
        LoggedInRendering(
          screen = rendering.screen.wrapWithBottomNav(renderState, context),
          overlay = rendering.overlay
        )
      }

      is Settings -> LoggedInRendering(
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
      snackbarMessage = renderState.trackerChanged?.message,
      onSnackbarAcknowledged = context.eventHandler {
        state = state.with(trackerChanged = null)
      },
      currentDestination = renderState.asDestination(),
      onDestinationChanged = context.eventHandler { destination: Destination ->
        state = when (destination) {
          Destination.Trackers -> Trackers(trackerChanged = state.trackerChanged)
          Destination.Friends -> Friends(state.trackerChanged)
          Destination.Settings -> Settings(state.trackerChanged)
        }
      }
    )
  }

  private fun State.asDestination(): Destination {
    return when (this) {
      is Trackers -> Destination.Trackers
      is Friends -> Destination.Friends
      is Settings -> Destination.Settings
    }
  }
}