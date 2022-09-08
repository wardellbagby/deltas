package com.wardellbagby.tracks.android.settings

import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.ScreenAndOverlay
import com.wardellbagby.tracks.android.Toaster
import com.wardellbagby.tracks.android.asScreenAndOverlay
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.core_ui.asOverlay
import com.wardellbagby.tracks.android.firebase.asWorker
import com.wardellbagby.tracks.android.networking.Endpoint
import com.wardellbagby.tracks.android.settings.SettingsWorkflow.State
import com.wardellbagby.tracks.android.settings.SettingsWorkflow.State.EnteringDisplayName
import com.wardellbagby.tracks.android.settings.SettingsWorkflow.State.Idle
import com.wardellbagby.tracks.android.settings.SettingsWorkflow.State.SavingDisplayName
import com.wardellbagby.tracks.android.strings.asTextData
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class SettingsWorkflow
@Inject constructor(
  private val endpoint: Endpoint,
  private val toaster: Toaster
) : StatefulWorkflow<Unit, State, Nothing, ScreenAndOverlay>() {
  sealed interface State : Parcelable {
    @Parcelize
    object Idle : State

    @Parcelize
    data class EnteringDisplayName(
      val originalDisplayName: String,
      val displayNameTextController: ParcelableTextController
    ) : State

    @Parcelize
    data class SavingDisplayName(
      val displayName: String
    ) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: Idle
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): ScreenAndOverlay {
    return when (renderState) {
      is EnteringDisplayName -> emptySettingsScreen().asScreenAndOverlay(
        overlay = EnteringDisplayNameScreen(
          originalDisplayName = renderState.originalDisplayName,
          displayNameTextController = renderState.displayNameTextController,
          onBack = context.eventHandler {
            state = Idle
          },
          onSaveClicked = context.eventHandler {
            val currentState = state as? EnteringDisplayName ?: return@eventHandler
            state = SavingDisplayName(
              displayName = currentState.displayNameTextController.textValue
            )
          }
        ).asOverlay()
      )

      is SavingDisplayName -> {
        context.runningWorker(
          Firebase.auth.currentUser!!
            .updateProfile(
              UserProfileChangeRequest.Builder()
                .setDisplayName(renderState.displayName)
                .build()
            ).asWorker()
        ) {
          action {
            state = Idle
            if (it.isFailure) {
              toaster.showToast(R.string.failed_to_update_display_name.asTextData())
            }
          }
        }
        LoadingScreen.asScreenAndOverlay()
      }

      Idle -> SettingsScreen(
        displayName = Firebase.auth.currentUser!!.displayName?.asTextData()
          ?: R.string.no_display_name.asTextData(),
        endpoint = endpoint.current,
        onDisplayNameClicked = context.eventHandler {
          val displayName = Firebase.auth.currentUser!!.displayName.orEmpty()
          state = EnteringDisplayName(
            originalDisplayName = displayName,
            displayNameTextController = ParcelableTextController(displayName)
          )
        },
        onEndpointChanged = context.eventHandler { new ->
          endpoint.current = new
        },
        onLogoutClicked = context.eventHandler {
          FirebaseAuth.getInstance().signOut()
        }
      ).asScreenAndOverlay()
    }
  }

  private fun emptySettingsScreen(): SettingsScreen {
    return SettingsScreen(
      displayName = Firebase.auth.currentUser!!.displayName?.asTextData()
        ?: R.string.no_display_name.asTextData(),
      endpoint = endpoint.current,
      onDisplayNameClicked = {},
      onEndpointChanged = {},
      onLogoutClicked = {}
    )
  }
}