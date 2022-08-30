package com.wardellbagby.tracks.android.settings

import com.google.firebase.auth.FirebaseAuth
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.ui.Screen
import javax.inject.Inject

class SettingsWorkflow
@Inject constructor() : StatelessWorkflow<Unit, Nothing, Screen>() {
  override fun render(
    renderProps: Unit,
    context: RenderContext
  ): Screen {
    return SettingsScreen(
      onLogoutClicked = context.eventHandler {
        FirebaseAuth.getInstance().signOut()
      }
    )
  }
}