package com.wardellbagby.deltas.android.auth

import android.os.Parcelable
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.BuildConfig
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.Toaster
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.CreatingAccountWithEmailPassword
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.EnteringEmail
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.EnteringEmailPassword
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.SendingSignInLink
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.SigningInWithEmailLink
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.SigningInWithEmailPassword
import com.wardellbagby.deltas.android.auth.AuthWorkflow.State.WaitingForSignInLinkResult
import com.wardellbagby.deltas.android.core_ui.LoadingScreen
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler
import com.wardellbagby.deltas.android.deeplinks.DeepLinkHandler.DeepLinkResult.LoginAttempt
import com.wardellbagby.deltas.android.firebase.asWorker
import com.wardellbagby.deltas.android.strings.asTextData
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

private val emailLinkSettings = actionCodeSettings {
  url = "${BuildConfig.DEEP_LINK_HOST}://${BuildConfig.DEEP_LINK_FIREBASE_HOST}"
  handleCodeInApp = true
  setAndroidPackageName(
    BuildConfig.APPLICATION_ID,
    true,
    null
  )
}

class AuthWorkflow
@Inject constructor(
  private val deepLinkHandler: DeepLinkHandler,
  private val toaster: Toaster
) : StatefulWorkflow<Unit, State, Nothing, Screen>() {
  sealed interface State : Parcelable {
    @Parcelize
    data class EnteringEmail(
      val emailTextController: ParcelableTextController = ParcelableTextController(),
      val link: String? = null
    ) : State

    @Parcelize
    data class SendingSignInLink(val email: String) : State

    @Parcelize
    data class WaitingForSignInLinkResult(val email: String) : State

    @Parcelize
    data class SigningInWithEmailLink(val email: String, val link: String) : State

    @Parcelize
    data class SigningInWithEmailPassword(val email: String, val password: String) : State

    @Parcelize
    data class CreatingAccountWithEmailPassword(val email: String, val password: String) : State

    @Parcelize
    data class EnteringEmailPassword(
      val emailTextController: ParcelableTextController = ParcelableTextController(),
      val passwordTextController: ParcelableTextController = ParcelableTextController()
    ) : State
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: EnteringEmail()
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): Screen {
    return when (renderState) {
      is EnteringEmail -> {
        context.runningWorker(
          deepLinkHandler.currentDeepLink
            .filterIsInstance<LoginAttempt>()
            .asWorker()
        ) {
          action {
            deepLinkHandler.onDeepLinkHandled()

            val currentState = state as? EnteringEmail ?: return@action
            state = EnteringEmail(
              emailTextController = currentState.emailTextController,
              link = it.data
            )
            toaster.showToast(R.string.please_reenter_email.asTextData())
          }
        }

        SignInScreen(
          emailTextController = renderState.emailTextController,
          onSignIn = context.eventHandler {
            val currentState = state as? EnteringEmail ?: return@eventHandler

            state = if (currentState.link.isNullOrBlank()) {
              SendingSignInLink(
                email = currentState.emailTextController.textValue
              )
            } else {
              SigningInWithEmailLink(
                email = currentState.emailTextController.textValue,
                link = currentState.link
              )
            }
          },
          onEmailPassword = context.eventHandler {
            val currentState = state as? EnteringEmail ?: return@eventHandler

            state = EnteringEmailPassword(
              emailTextController = currentState.emailTextController
            )
          }
        )
      }

      is SendingSignInLink -> {
        context.runningWorker(renderState.sendSignInLinkWorker()) {
          action {
            val currentState = state as? SendingSignInLink ?: return@action
            if (it.isFailure) {
              Log.e("AuthWorkflow", "Failed to send sign in link", it.exceptionOrNull())
            }

            state = WaitingForSignInLinkResult(email = currentState.email)
          }
        }
        LoadingScreen
      }

      is WaitingForSignInLinkResult -> {
        context.runningWorker(
          deepLinkHandler.currentDeepLink
            .filterIsInstance<LoginAttempt>()
            .asWorker()
        ) {
          action {
            val currentState = state as? WaitingForSignInLinkResult ?: return@action
            state = SigningInWithEmailLink(
              email = currentState.email,
              link = it.data
            )
          }
        }

        WaitingForSignInLinkScreen(
          onTryAgain = context.eventHandler {
            state = EnteringEmail()
          }
        )
      }

      is SigningInWithEmailLink -> {
        context.runningWorker(
          Firebase.auth.signInWithEmailLink(
            renderState.email,
            renderState.link
          ).asWorker()
        ) {
          action {
            if (it.isAuthFailure) {
              state = EnteringEmail()
              Log.e("AuthWorkflow", "Login failure", it.exceptionOrNull())
              toaster.showToast(R.string.failed_to_login.asTextData())
            }
          }
        }
        LoadingScreen
      }

      is EnteringEmailPassword -> EnteringEmailPasswordScreen(
        emailTextController = renderState.emailTextController,
        passwordTextController = renderState.passwordTextController,
        onSignIn = context.eventHandler {
          val currentState = state as? EnteringEmailPassword ?: return@eventHandler
          state = SigningInWithEmailPassword(
            email = currentState.emailTextController.textValue,
            password = currentState.passwordTextController.textValue
          )
        },
        onCreateAccount = context.eventHandler {
          val currentState = state as? EnteringEmailPassword ?: return@eventHandler
          state = CreatingAccountWithEmailPassword(
            email = currentState.emailTextController.textValue,
            password = currentState.passwordTextController.textValue
          )
        },
        onUseEmailLink = context.eventHandler {
          val currentState = state as? EnteringEmailPassword ?: return@eventHandler
          state = EnteringEmail(
            emailTextController = currentState.emailTextController
          )
        }
      )

      is SigningInWithEmailPassword -> {
        context.runningWorker(renderState.createSignInWorker()) {
          action {
            val currentState = state as? SigningInWithEmailPassword ?: return@action
            if (it.isAuthFailure) {
              state = EnteringEmailPassword(
                emailTextController = ParcelableTextController(currentState.email)
              )
              Log.e("AuthWorkflow", "Login failure", it.exceptionOrNull())
              toaster.showToast(R.string.failed_to_login.asTextData())
            }
          }
        }
        LoadingScreen
      }

      is CreatingAccountWithEmailPassword -> {
        context.runningWorker(renderState.createAccountCreationWorker()) {
          action {
            val currentState = state as? CreatingAccountWithEmailPassword ?: return@action
            if (it.isAuthFailure) {
              state = EnteringEmailPassword(
                emailTextController = ParcelableTextController(currentState.email)
              )
              Log.e("AuthWorkflow", "Account creation failure", it.exceptionOrNull())
              toaster.showToast(R.string.failed_to_create_account.asTextData())
            }
          }
        }
        LoadingScreen
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  private fun SendingSignInLink.sendSignInLinkWorker() =
    FirebaseAuth.getInstance()
      .sendSignInLinkToEmail(email, emailLinkSettings)
      .asWorker()

  private fun SigningInWithEmailPassword.createSignInWorker() =
    FirebaseAuth.getInstance()
      .signInWithEmailAndPassword(email, password)
      .asWorker()

  private fun CreatingAccountWithEmailPassword.createAccountCreationWorker() =
    FirebaseAuth.getInstance()
      .createUserWithEmailAndPassword(email, password)
      .asWorker()

  private val Result<AuthResult>.isAuthFailure
    get() = isFailure || getOrNull()?.user == null
}