package com.wardellbagby.deltas.android.trackers.detail.sharing

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.ScreenAndOverlay
import com.wardellbagby.deltas.android.asScreenAndOverlay
import com.wardellbagby.deltas.android.core_ui.FailureScreen
import com.wardellbagby.deltas.android.core_ui.LoadingScreen
import com.wardellbagby.deltas.android.friends.Friend
import com.wardellbagby.deltas.android.friends.FriendsService
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow
import com.wardellbagby.deltas.android.friends.toModels
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.android.networking.mapResponse
import com.wardellbagby.deltas.android.strings.TextData
import com.wardellbagby.deltas.android.strings.asTextData
import com.wardellbagby.deltas.android.trackers.TrackerService
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State.AddingFriend
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State.ChoosingFriends
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State.Failure
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State.LoadingFriends
import com.wardellbagby.deltas.android.trackers.detail.sharing.SharePrivateTrackerWorkflow.State.Sharing
import com.wardellbagby.deltas.android.trackers.models.Tracker
import com.wardellbagby.deltas.models.trackers.UpdateTrackerRequest
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class SharePrivateTrackerWorkflow
@Inject constructor(
  private val friendsService: FriendsService,
  private val trackerService: TrackerService,
  private val addFriendWorkflow: AddFriendWorkflow
) : StatefulWorkflow<Tracker, State, Unit, ScreenAndOverlay>() {
  sealed interface State : Parcelable {
    val allFriends: List<Friend>

    @Parcelize
    object LoadingFriends : State {
      override val allFriends: List<Friend>
        get() = emptyList()
    }

    @Parcelize
    data class Failure(val message: TextData) : State {
      override val allFriends: List<Friend>
        get() = emptyList()
    }

    @Parcelize
    data class ChoosingFriends(
      override val allFriends: List<Friend>,
      val selectedFriends: List<Friend> = emptyList()
    ) : State

    @Parcelize
    data class Sharing(
      val selectedFriends: List<Friend>
    ) : State {
      override val allFriends: List<Friend>
        get() = emptyList()
    }

    @Parcelize
    data class AddingFriend(
      override val allFriends: List<Friend>
    ) : State
  }

  override fun initialState(props: Tracker, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: LoadingFriends
  }

  override fun render(
    renderProps: Tracker,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {
    return when (renderState) {
      LoadingFriends -> {
        context.runningWorker(Worker.from {
          friendsService.listFriends().mapResponse { it.friends.toModels() }
        }) {
          when (it) {
            is NetworkResult.Failure -> action {
              state = Failure(R.string.failed_to_load_friends.asTextData())
            }

            is NetworkResult.Success -> action {
              state = ChoosingFriends(allFriends = it.response, selectedFriends = props.visibleTo)
            }
          }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is ChoosingFriends -> ChoosingFriendsScreen(
        allFriends = renderState.allFriends,
        selectedFriends = renderState.selectedFriends,
        onFriendSelected = context.eventHandler { friend ->
          val currentState = state as? ChoosingFriends ?: return@eventHandler
          state = if (friend in currentState.selectedFriends) {
            currentState.copy(
              selectedFriends = currentState.selectedFriends.dropWhile { it.id == friend.id }
            )
          } else {
            currentState.copy(
              selectedFriends = (currentState.selectedFriends + friend).distinctBy { it.id }
            )
          }
        },
        onAddFriendClicked = context.eventHandler {
          state = AddingFriend(state.allFriends)
        },
        onShareClicked = context.eventHandler {
          val currentState = state as? ChoosingFriends ?: return@eventHandler

          state = Sharing(selectedFriends = currentState.selectedFriends)
        }
      ).asScreenAndOverlay()

      is Failure -> FailureScreen(
        message = renderState.message,
        onRetry = context.eventHandler {
          state = LoadingFriends
        }
      ).asScreenAndOverlay()

      is Sharing -> {
        context.runningWorker(Worker.from {
          trackerService.updateTracker(
            UpdateTrackerRequest(
              id = renderProps.id,
              idsToShareWith = renderState.selectedFriends.map { it.id }
            )
          )
        }) {
          when (it) {
            is NetworkResult.Failure -> action {
              state = Failure(R.string.failed_to_share_tracker.asTextData())
            }

            is NetworkResult.Success -> action { setOutput(Unit) }
          }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      is AddingFriend -> context.renderChild(addFriendWorkflow) {
        action { state = LoadingFriends }
      }.let {
        ScreenAndOverlay(
          screen = ChoosingFriendsScreen(
            allFriends = renderState.allFriends,
            selectedFriends = emptyList(),
            onFriendSelected = {},
            onAddFriendClicked = {},
            onShareClicked = {}
          ),
          overlay = it
        )
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}