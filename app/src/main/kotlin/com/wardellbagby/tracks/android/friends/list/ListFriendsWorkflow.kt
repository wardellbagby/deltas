package com.wardellbagby.tracks.android.friends.list

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.FailureScreen
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.friends.Friend
import com.wardellbagby.tracks.android.friends.FriendsService
import com.wardellbagby.tracks.android.friends.list.ListFriendsWorkflow.State
import com.wardellbagby.tracks.android.friends.list.ListFriendsWorkflow.State.DisplayingFriends
import com.wardellbagby.tracks.android.friends.list.ListFriendsWorkflow.State.Failure
import com.wardellbagby.tracks.android.friends.list.ListFriendsWorkflow.State.LoadingFriends
import com.wardellbagby.tracks.android.friends.toModel
import com.wardellbagby.tracks.android.networking.NetworkResult
import com.wardellbagby.tracks.android.networking.mapResponse
import com.wardellbagby.tracks.android.strings.TextData
import com.wardellbagby.tracks.android.strings.asTextData
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class ListFriendsWorkflow
@Inject constructor(
  private val service: FriendsService
) : StatefulWorkflow<Unit, State, Nothing, Screen>() {

  sealed interface State : Parcelable {
    @Parcelize
    object LoadingFriends : State

    @Parcelize
    data class DisplayingFriends(val friends: List<Friend>) : State

    @Parcelize
    data class Failure(val message: TextData) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State =
    snapshot?.toParcelable() ?: LoadingFriends

  override fun render(renderProps: Unit, renderState: State, context: RenderContext): Screen {
    // TODO Paginate
    return when (renderState) {
      is DisplayingFriends -> ListFriendsScreen(
        friends = renderState.friends
      )
      is Failure -> FailureScreen(
        title = R.string.unexpected_error.asTextData(),
        message = renderState.message,
        onRetry = context.eventHandler {
          state = LoadingFriends
        }
      )
      LoadingFriends -> {
        context.runningWorker(Worker.from {
          service.listFriends().mapResponse { it.friends.map { friend -> friend.toModel() } }
        }) {
          action {
            state = when (it) {
              is NetworkResult.Failure -> {
                Failure(message = R.string.failed_to_load_friends.asTextData())
              }
              is NetworkResult.Success -> {
                DisplayingFriends(friends = it.response)
              }
            }
          }
        }
        LoadingScreen
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}