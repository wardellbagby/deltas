package com.wardellbagby.tracks.android.trackers.detail

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.ActivityProvider
import com.wardellbagby.tracks.android.ScreenAndOverlay
import com.wardellbagby.tracks.android.asScreenAndOverlay
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.core_ui.asOverlay
import com.wardellbagby.tracks.android.trackers.TrackerService
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.Props
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Deleting
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Sharing
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.TypingUpdateLabel
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Unsubscribing
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Updating
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Viewing
import com.wardellbagby.tracks.android.trackers.detail.history.ListTrackerHistoryWorkflow
import com.wardellbagby.tracks.android.trackers.detail.sharing.SharePrivateTrackerWorkflow
import com.wardellbagby.tracks.android.trackers.models.Tracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.ElapsedTimeTracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.IncrementalTracker
import com.wardellbagby.tracks.android.trackers.models.type
import com.wardellbagby.tracks.models.trackers.DeleteTrackerRequest
import com.wardellbagby.tracks.models.trackers.TrackerVisibility.Private
import com.wardellbagby.tracks.models.trackers.TrackerVisibility.Public
import com.wardellbagby.tracks.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.tracks.models.trackers.UpdateTrackerRequest
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class TrackerDetailsWorkflow
@Inject constructor(
  private val service: TrackerService,
  private val activityProvider: ActivityProvider,
  private val sharePrivateTrackerWorkflow: SharePrivateTrackerWorkflow,
  private val trackerHistoryWorkflow: ListTrackerHistoryWorkflow
) : StatefulWorkflow<Props, State, Unit, ScreenAndOverlay>() {
  data class Props(
    val tracker: Tracker
  )

  @Parcelize
  data class UpdateRequest(
    val shouldResetTime: Boolean = false,
    val shouldIncrementCount: Boolean = false,
    val idsToShareWith: List<String>? = null,
    val label: String? = null
  ) : Parcelable

  private fun UpdateRequest.toDTO(props: Props) = UpdateTrackerRequest(
    id = props.tracker.id,
    shouldResetTime = shouldResetTime,
    shouldIncrementCount = shouldIncrementCount,
    idsToShareWith = idsToShareWith,
    label = label
  )

  sealed interface State : Parcelable {
    @Parcelize
    object Viewing : State

    @Parcelize
    data class Updating(val request: UpdateRequest) : State

    @Parcelize
    object Deleting : State

    @Parcelize
    object Unsubscribing : State

    @Parcelize
    object Sharing : State

    @Parcelize
    data class TypingUpdateLabel(
      val labelTextController: ParcelableTextController = ParcelableTextController()
    ) : State
  }

  override fun initialState(props: Props, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: Viewing
  }

  override fun render(
    renderProps: Props,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {
    return when (renderState) {
      is Updating -> {
        context.runningWorker(Worker.from {
          service.updateTracker(renderState.request.toDTO(renderProps))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Deleting -> {
        context.runningWorker(Worker.from {
          service.deleteTracker(DeleteTrackerRequest(id = renderProps.tracker.id))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Unsubscribing -> {
        context.runningWorker(Worker.from {
          service.unsubscribeTracker(UnsubscribeTrackerRequest(id = renderProps.tracker.id))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Sharing -> {
        when (renderProps.tracker.visibility) {
          Private -> context.renderChild(
            sharePrivateTrackerWorkflow,
            props = renderProps.tracker
          ) {
            action { setOutput(Unit) }
          }

          Public -> {
            context.runningWorker(
              Worker.from {
                activityProvider.activity?.startActivity(
                  Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, renderProps.tracker.asDeepLink())
                  }
                )
              }) {
              action {
                state = Viewing
              }
            }

            trackerDetailScreen(renderProps, context)
          }
        }
      }

      is TypingUpdateLabel -> {
        TrackerDetailsScreen(
          tracker = renderProps.tracker,
          historyRendering = context.renderChild(
            trackerHistoryWorkflow,
            props = renderProps.tracker.id
          ),
          onShareClicked = {},
          onIncrementClicked = {},
          onResetTimeClicked = {},
          onDeleteClicked = {},
          onUnsubscribeClicked = {},
          onBack = {}
        ).asScreenAndOverlay(
          TypingUpdateLabelScreen(
            labelTextController = renderState.labelTextController,
            type = renderProps.tracker.type,
            onBack = context.eventHandler {
              state = Viewing
            },
            onUpdateClicked = context.eventHandler {
              val currentState = state as? TypingUpdateLabel ?: return@eventHandler

              state = Updating(
                UpdateRequest(
                  shouldResetTime = props.tracker is ElapsedTimeTracker,
                  shouldIncrementCount = props.tracker is IncrementalTracker,
                  label = currentState.labelTextController.textValue
                )
              )
            }
          ).asOverlay()
        )
      }

      Viewing -> trackerDetailScreen(renderProps, context)
    }
  }

  private fun trackerDetailScreen(renderProps: Props, context: RenderContext): ScreenAndOverlay {
    return TrackerDetailsScreen(
      tracker = renderProps.tracker,
      historyRendering = context.renderChild(
        trackerHistoryWorkflow,
        props = renderProps.tracker.id
      ),
      onShareClicked = context.eventHandler {
        state = Sharing
      },
      onIncrementClicked = context.eventHandler {
        state = TypingUpdateLabel()
      },
      onResetTimeClicked = context.eventHandler {
        state = TypingUpdateLabel()
      },
      onDeleteClicked = context.eventHandler {
        state = Deleting
      },
      onUnsubscribeClicked = context.eventHandler {
        state = Unsubscribing
      },
      onBack = context.eventHandler {
        setOutput(Unit)
      }
    ).asScreenAndOverlay()
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  private fun Tracker.asDeepLink(): String {
    return Uri.fromParts("tracks", "view/$id", null).toString()
  }
}