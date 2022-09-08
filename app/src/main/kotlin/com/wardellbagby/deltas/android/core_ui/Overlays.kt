package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.container.AndroidOverlay
import com.squareup.workflow1.ui.container.ModalOverlay
import com.squareup.workflow1.ui.container.Overlay
import com.squareup.workflow1.ui.container.OverlayDialogFactory
import com.squareup.workflow1.ui.container.ScreenOverlay
import com.squareup.workflow1.ui.container.ScreenOverlayDialogFactory
import com.wardellbagby.deltas.android.theming.OverlayCompositionRoot

private data class WrappedWithBackground(
  val wrapped: Screen
) : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    OverlayCompositionRoot {
      WorkflowRendering(
        wrapped.withContentPadding(
          values = PaddingValues(32.dp),
          environment = viewEnvironment
        ),
        viewEnvironment
      )
    }
  }
}

private class ScreenAsOverlay(
  wrapped: Screen
) : ScreenOverlay<Screen>, ModalOverlay, AndroidOverlay<ScreenAsOverlay> {
  override val dialogFactory: OverlayDialogFactory<ScreenAsOverlay> =
    ScreenOverlayDialogFactory(type = ScreenAsOverlay::class)

  override val content: Screen = WrappedWithBackground(wrapped)
}

fun <T : Screen> T.asOverlay(): Overlay {
  return ScreenAsOverlay(this)
}