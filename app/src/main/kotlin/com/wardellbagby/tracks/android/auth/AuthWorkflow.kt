package com.wardellbagby.tracks.android.auth

import android.os.Parcelable
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.Toaster
import com.wardellbagby.tracks.android.auth.AuthWorkflow.State
import com.wardellbagby.tracks.android.auth.AuthWorkflow.State.CreatingAccount
import com.wardellbagby.tracks.android.auth.AuthWorkflow.State.Idle
import com.wardellbagby.tracks.android.auth.AuthWorkflow.State.LoggingIn
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.firebase.asWorker
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class AuthWorkflow
@Inject constructor(
  private val toaster: Toaster
) : StatefulWorkflow<Unit, State, Nothing, Screen>() {
  sealed interface State : Parcelable {
    @Parcelize
    data class Idle(
      val emailTextController: ParcelableTextController = ParcelableTextController(),
      val passwordTextController: ParcelableTextController = ParcelableTextController()
    ) : State

    @Parcelize
    data class LoggingIn(
      val email: String,
      val password: String
    ) : State

    @Parcelize
    data class CreatingAccount(
      val email: String,
      val password: String
    ) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: Idle()
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): Screen {
    return when (renderState) {
      is Idle -> SignInScreen(
        emailTextController = renderState.emailTextController,
        passwordTextController = renderState.passwordTextController,
        onSignIn = context.eventHandler {
          val currentState = state as? Idle ?: return@eventHandler

          state = LoggingIn(
            email = currentState.emailTextController.textValue,
            password = currentState.passwordTextController.textValue
          )
        },
        onCreateAccount = context.eventHandler {
          val currentState = state as? Idle ?: return@eventHandler

          state = CreatingAccount(
            email = currentState.emailTextController.textValue,
            password = currentState.passwordTextController.textValue
          )
        }
      )
      is LoggingIn -> {
        context.runningWorker(renderState.createLoginWorker()) {
          action {
            val currentState = state as? LoggingIn ?: return@action

            if (it.isAuthFailure) {
              toaster.showToast("Failed to login with the provided credentials")
              state = Idle(
                emailTextController = ParcelableTextController(currentState.email),
                passwordTextController = ParcelableTextController(currentState.password)
              )
            }
          }
        }
        LoadingScreen
      }
      is CreatingAccount -> {
        context.runningWorker(renderState.createAccountCreationWorker()) {
          action {
            if (it.isAuthFailure) {
              toaster.showToast("Failed to create account!")
              state = Idle()
            }
          }
        }
        LoadingScreen
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  private fun LoggingIn.createLoginWorker() =
    FirebaseAuth.getInstance()
      .signInWithEmailAndPassword(email, password)
      .asWorker()

  private fun CreatingAccount.createAccountCreationWorker() =
    FirebaseAuth.getInstance()
      .createUserWithEmailAndPassword(email, password)
      .asWorker()

  private val Result<AuthResult>.isAuthFailure
    get() = isFailure || getOrNull()?.user == null
}