package com.wardellbagby.tracks.android.settings

import com.google.firebase.auth.FirebaseAuth
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.ui.Screen
import com.wardellbagby.tracks.android.networking.Endpoint
import javax.inject.Inject

class SettingsWorkflow
@Inject constructor(
  private val endpoint: Endpoint
) : StatelessWorkflow<Unit, Nothing, Screen>() {
  override fun render(
    renderProps: Unit,
    context: RenderContext
  ): Screen {
    return SettingsScreen(
      endpoint = endpoint.current,
      onEndpointChanged = context.eventHandler { new ->
        endpoint.current = new
      },
      onLogoutClicked = context.eventHandler {
        FirebaseAuth.getInstance().signOut()
      }
    )
  }
}