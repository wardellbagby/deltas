package com.wardellbagby.deltas.android.friends

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.ScreenAndOverlay
import com.wardellbagby.deltas.android.friends.FriendsWorkflow.State
import com.wardellbagby.deltas.android.friends.FriendsWorkflow.State.AddFriend
import com.wardellbagby.deltas.android.friends.FriendsWorkflow.State.List
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow
import com.wardellbagby.deltas.android.friends.list.ListFriendsWorkflow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class FriendsWorkflow
@Inject constructor(
  private val listWorkflow: ListFriendsWorkflow,
  private val addFriendWorkflow: AddFriendWorkflow
) : StatefulWorkflow<Unit, State, Nothing, ScreenAndOverlay>() {
  sealed interface State : Parcelable {
    val listKey: Int

    @Parcelize
    data class List(override val listKey: Int = 0) : State

    @Parcelize
    data class AddFriend(override val listKey: Int) : State
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {

    val listRendering = context.renderChild(
      listWorkflow,
      key = renderState.listKey.toString()
    )
    return ScreenAndOverlay(
      screen = FriendsScreen(
        list = listRendering,
        onAddClicked = context.eventHandler {
          state = AddFriend(state.listKey)
        }
      ),
      overlay = (renderState as? AddFriend)?.let {
        context.renderChild(addFriendWorkflow) {
          action { state = List(state.listKey + 1) }
        }
      }
    )
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: List()
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}