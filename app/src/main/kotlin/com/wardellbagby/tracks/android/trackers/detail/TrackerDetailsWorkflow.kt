package com.wardellbagby.tracks.android.trackers.detail

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
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.ScreenAndOverlay
import com.wardellbagby.tracks.android.asScreenAndOverlay
import com.wardellbagby.tracks.android.core_ui.FailureScreen
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.core_ui.asOverlay
import com.wardellbagby.tracks.android.deeplinks.DeepLinkCreator
import com.wardellbagby.tracks.android.networking.NetworkResult.Failure
import com.wardellbagby.tracks.android.networking.NetworkResult.Success
import com.wardellbagby.tracks.android.strings.TextData
import com.wardellbagby.tracks.android.strings.TextData.ResourceText
import com.wardellbagby.tracks.android.strings.asTextData
import com.wardellbagby.tracks.android.trackers.TrackerService
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.Props
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.Props.FullTracker
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.Props.TrackerId
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Deleting
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Loading
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.LoadingFailure
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Sharing
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Subscribing
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.TypingUpdateLabel
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Unsubscribing
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Updating
import com.wardellbagby.tracks.android.trackers.detail.TrackerDetailsWorkflow.State.Viewing
import com.wardellbagby.tracks.android.trackers.detail.history.ListTrackerHistoryWorkflow
import com.wardellbagby.tracks.android.trackers.detail.sharing.SharePrivateTrackerWorkflow
import com.wardellbagby.tracks.android.trackers.models.Tracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.ElapsedTimeTracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.IncrementalTracker
import com.wardellbagby.tracks.android.trackers.models.asModel
import com.wardellbagby.tracks.android.trackers.models.type
import com.wardellbagby.tracks.models.trackers.DeleteTrackerRequest
import com.wardellbagby.tracks.models.trackers.GetTrackerRequest
import com.wardellbagby.tracks.models.trackers.SubscribeTrackerRequest
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
  private val deepLinkCreator: DeepLinkCreator,
  private val sharePrivateTrackerWorkflow: SharePrivateTrackerWorkflow,
  private val trackerHistoryWorkflow: ListTrackerHistoryWorkflow
) : StatefulWorkflow<Props, State, Unit, ScreenAndOverlay>() {

  sealed interface Props {
    data class FullTracker(
      val tracker: Tracker
    ) : Props

    data class TrackerId(
      val id: String
    ) : Props
  }

  @Parcelize
  data class UpdateRequest(
    val shouldResetTime: Boolean = false,
    val shouldIncrementCount: Boolean = false,
    val idsToShareWith: List<String>? = null,
    val label: String? = null
  ) : Parcelable

  private fun UpdateRequest.toDTO(state: State) = UpdateTrackerRequest(
    id = state.tracker.id,
    shouldResetTime = shouldResetTime,
    shouldIncrementCount = shouldIncrementCount,
    idsToShareWith = idsToShareWith,
    label = label
  )

  sealed interface State : Parcelable {
    val tracker: Tracker

    @Parcelize
    data class Viewing(
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Updating(
      val request: UpdateRequest,
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Deleting(
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Unsubscribing(
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Subscribing(
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Sharing(
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class TypingUpdateLabel(
      val labelTextController: ParcelableTextController = ParcelableTextController(),
      override val tracker: Tracker
    ) : State

    @Parcelize
    data class Loading(val id: String) : State {
      override val tracker: Tracker
        get() = error("Loading state has no tracker")
    }

    @Parcelize
    data class LoadingFailure(
      val message: TextData
    ) : State {
      override val tracker: Tracker
        get() = error("Loading state has no tracker")
    }
  }

  override fun initialState(props: Props, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: when (props) {
      is FullTracker -> Viewing(props.tracker)
      is TrackerId -> Loading(props.id)
    }
  }

  override fun render(
    renderProps: Props,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {
    return when (renderState) {
      is Loading -> {
        context.runningWorker(
          Worker.from {
            service.getTracker(
              GetTrackerRequest(
                id = renderState.id
              )
            )
          }
        ) {
          when (it) {
            is Failure -> action {
              state = LoadingFailure(
                it.message?.asTextData() ?: ResourceText(R.string.failed_to_load_tracker)
              )
            }

            is Success -> action {
              state = Viewing(it.response.tracker!!.asModel())
            }
          }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is LoadingFailure ->
        FailureScreen(message = renderState.message)
          .asScreenAndOverlay()

      is Updating -> {
        context.runningWorker(Worker.from {
          service.updateTracker(renderState.request.toDTO(renderState))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Deleting -> {
        context.runningWorker(Worker.from {
          service.deleteTracker(DeleteTrackerRequest(id = renderState.tracker.id))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Unsubscribing -> {
        context.runningWorker(Worker.from {
          service.unsubscribeTracker(UnsubscribeTrackerRequest(id = renderState.tracker.id))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Subscribing -> {
        context.runningWorker(Worker.from {
          service.subscribeTracker(SubscribeTrackerRequest(id = renderState.tracker.id))
        }) {
          action { setOutput(Unit) }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is Sharing -> {
        when (renderState.tracker.visibility) {
          Private -> context.renderChild(
            sharePrivateTrackerWorkflow,
            props = renderState.tracker
          ) {
            action { setOutput(Unit) }
          }

          Public -> {
            context.runningWorker(
              Worker.from {
                activityProvider.activity?.startActivity(
                  deepLinkCreator.createShareTrackerIntent(renderState.tracker)
                )
              }) {
              action {
                state = Viewing(state.tracker)
              }
            }

            trackerDetailScreen(renderState, context)
          }
        }
      }

      is TypingUpdateLabel -> {
        TrackerDetailsScreen(
          tracker = renderState.tracker,
          historyRendering = context.renderChild(
            trackerHistoryWorkflow,
            props = renderState.tracker.id
          ),
          onShareClicked = {},
          onIncrementClicked = {},
          onResetTimeClicked = {},
          onDeleteClicked = {},
          onSubscribeClicked = {},
          onUnsubscribeClicked = {},
          onBack = {}
        ).asScreenAndOverlay(
          TypingUpdateLabelScreen(
            labelTextController = renderState.labelTextController,
            type = renderState.tracker.type,
            onBack = context.eventHandler {
              state = Viewing(state.tracker)
            },
            onUpdateClicked = context.eventHandler {
              val currentState = state as? TypingUpdateLabel ?: return@eventHandler

              state = Updating(
                UpdateRequest(
                  shouldResetTime = state.tracker is ElapsedTimeTracker,
                  shouldIncrementCount = state.tracker is IncrementalTracker,
                  label = currentState.labelTextController.textValue
                ),
                tracker = state.tracker
              )
            }
          ).asOverlay()
        )
      }

      is Viewing -> trackerDetailScreen(renderState, context)
    }
  }

  private fun trackerDetailScreen(renderState: State, context: RenderContext): ScreenAndOverlay {
    return TrackerDetailsScreen(
      tracker = renderState.tracker,
      historyRendering = context.renderChild(
        trackerHistoryWorkflow,
        props = renderState.tracker.id
      ),
      onShareClicked = context.eventHandler {
        state = Sharing(state.tracker)
      },
      onIncrementClicked = context.eventHandler {
        state = TypingUpdateLabel(tracker = state.tracker)
      },
      onResetTimeClicked = context.eventHandler {
        state = TypingUpdateLabel(tracker = state.tracker)
      },
      onDeleteClicked = context.eventHandler {
        state = Deleting(tracker = state.tracker)
      },
      onSubscribeClicked = context.eventHandler {
        state = Subscribing(tracker = state.tracker)
      },
      onUnsubscribeClicked = context.eventHandler {
        state = Unsubscribing(tracker = state.tracker)
      },
      onBack = context.eventHandler {
        setOutput(Unit)
      }
    ).asScreenAndOverlay()
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}