package com.wardellbagby.deltas.android.trackers

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.ScreenAndOverlay
import com.wardellbagby.deltas.android.Toaster
import com.wardellbagby.deltas.android.asScreenAndOverlay
import com.wardellbagby.deltas.android.core_ui.LoadingScreen
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler.DeepLinkResult
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.android.strings.asTextData
import com.wardellbagby.deltas.android.trackers.TrackersWorkflow.State
import com.wardellbagby.deltas.android.trackers.TrackersWorkflow.State.Creating
import com.wardellbagby.deltas.android.trackers.TrackersWorkflow.State.Listing
import com.wardellbagby.deltas.android.trackers.TrackersWorkflow.State.SavingTracker
import com.wardellbagby.deltas.android.trackers.TrackersWorkflow.State.Viewing
import com.wardellbagby.deltas.android.trackers.creation.CreateTrackerWorkflow
import com.wardellbagby.deltas.android.trackers.detail.TrackerDetailsWorkflow
import com.wardellbagby.deltas.android.trackers.list.ListTrackersWorkflow
import com.wardellbagby.deltas.android.trackers.list.ListTrackersWorkflow.Output.TrackerClicked
import com.wardellbagby.deltas.android.trackers.models.Tracker
import com.wardellbagby.deltas.models.trackers.CreateTrackerRequest
import com.wardellbagby.deltas.models.trackers.TrackerType
import com.wardellbagby.deltas.models.trackers.TrackerVisibility
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class TrackersWorkflow
@Inject constructor(
  private val listWorkflow: ListTrackersWorkflow,
  private val createWorkflow: CreateTrackerWorkflow,
  private val detailsWorkflow: TrackerDetailsWorkflow,
  private val service: TrackerService,
  private val toaster: Toaster,
  private val deepLinkHandler: DeepLinkHandler
) : StatefulWorkflow<Unit, State, Nothing, ScreenAndOverlay>() {
  sealed interface State : Parcelable {

    @Parcelize
    object Listing : State

    @Parcelize
    object Creating : State

    @Parcelize
    data class SavingTracker(
      val type: TrackerType,
      val label: String,
      val visibility: TrackerVisibility
    ) : State

    @Parcelize
    data class Viewing(
      val tracker: Tracker? = null,
      val id: String? = null
    ) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State =
    snapshot?.toParcelable() ?: Listing

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {
    context.runningWorker(
      deepLinkHandler.currentDeepLink
        .filterIsInstance<DeepLinkResult.ViewTracker>()
        .asWorker()
    ) {
      action {
        state = Viewing(id = it.id)
        deepLinkHandler.onDeepLinkHandled()
      }
    }
    return when (renderState) {
      is Creating -> context.renderChild(createWorkflow) {
        when (it) {
          CreateTrackerWorkflow.Output.Cancelled -> action {
            state = Listing
          }

          is CreateTrackerWorkflow.Output.Created -> action {
            state =
              SavingTracker(type = it.type, label = it.label, visibility = it.visibility)
          }
        }
      }.let {
        TrackersChildScreen(
          title = R.string.create_tracker_title.asTextData(),
          rendering = it,
          onBack = context.eventHandler {
            state = Listing
          }).asScreenAndOverlay()
      }

      is Listing -> TrackersScreen(
        list = context.renderChild(child = listWorkflow) {
          when (it) {
            is TrackerClicked -> action {
              state = Viewing(tracker = it.tracker)
            }
          }
        },
        onCreateClicked = context.eventHandler {
          state = Creating
        }
      ).asScreenAndOverlay()

      is Viewing -> context.renderChild(
        detailsWorkflow,
        props = if (renderState.tracker != null) {
          TrackerDetailsWorkflow.Props.FullTracker(renderState.tracker)
        } else {
          TrackerDetailsWorkflow.Props.TrackerId(renderState.id!!)
        }
      ) {
        action { state = Listing }
      }.let {
        val title = (it.screen as? HasPageTitle)?.title ?: "".asTextData()

        TrackersChildScreen(
          title = title,
          rendering = it.screen,
          onBack = context.eventHandler {
            state = Listing
          }).asScreenAndOverlay(it.overlay)
      }

      is SavingTracker -> {
        // Keep rendering the create workflow so its state isn't lost in case
        // saving fails so we can transition back to it.
        context.renderChild(createWorkflow) {
          noAction()
        }

        context.runningWorker(Worker.from {
          service.createTracker(
            CreateTrackerRequest(
              type = renderState.type,
              label = renderState.label,
              visibility = renderState.visibility
            )
          )
        }) {
          when (it) {
            is NetworkResult.Failure -> action {
              state = Creating
              toaster.showToast(R.string.failed_to_save_counter.asTextData())
            }

            is NetworkResult.Success -> action {
              state = Listing
            }
          }
        }
        LoadingScreen.asScreenAndOverlay()
      }
    }
  }

  override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}