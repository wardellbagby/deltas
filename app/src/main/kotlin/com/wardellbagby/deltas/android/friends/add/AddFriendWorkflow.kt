package com.wardellbagby.deltas.android.friends.add

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.container.Overlay
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.core_ui.FailureScreen
import com.wardellbagby.deltas.android.core_ui.LoadingScreen
import com.wardellbagby.deltas.android.core_ui.asOverlay
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow.State
import com.wardellbagby.deltas.android.friends.FriendsService
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow.State.AddingFriend
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow.State.EnteringEmail
import com.wardellbagby.deltas.android.friends.add.AddFriendWorkflow.State.Failure
import com.wardellbagby.deltas.models.friends.AddFriendRequest
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.android.networking.asFailureWhen
import com.wardellbagby.deltas.android.strings.TextData
import com.wardellbagby.deltas.android.strings.asTextData
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class AddFriendWorkflow
@Inject constructor(
  private val service: FriendsService
) : StatefulWorkflow<Unit, State, Unit, Overlay>() {
  sealed interface State : Parcelable {
    val email: String

    @Parcelize
    data class EnteringEmail(
      val emailTextController: ParcelableTextController = ParcelableTextController()
    ) : State {
      override val email: String
        get() = emailTextController.textValue
    }

    @Parcelize
    data class AddingFriend(override val email: String) : State

    @Parcelize
    data class Failure(val message: TextData, override val email: String) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: EnteringEmail()
  }

  override fun render(renderProps: Unit, renderState: State, context: RenderContext): Overlay {
    return when (renderState) {
      is AddingFriend -> {
        context.runningWorker(
          Worker.from {
            service
              .addFriend(AddFriendRequest(email = renderState.email))
              .asFailureWhen { !it.success }
          }) {
          when (it) {
            is NetworkResult.Failure -> action {
              state = Failure(
                message = R.string.failed_to_add_friend.asTextData(),
                email = state.email
              )
            }

            is NetworkResult.Success -> action { setOutput(Unit) }
          }
        }
        LoadingScreen.asOverlay()
      }

      is EnteringEmail -> AddFriendScreen(
        emailTextController = renderState.emailTextController,
        onBack = context.eventHandler {
          setOutput(Unit)
        },
        onAddClicked = context.eventHandler {
          state = AddingFriend(email = state.email)
        }
      ).asOverlay()

      is Failure -> FailureScreen(
        message = renderState.message,
        onOk = context.eventHandler {
          setOutput(Unit)
        }
      ).asOverlay()
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}