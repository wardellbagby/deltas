package com.wardellbagby.deltas.android.loggedin

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.container.Overlay
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.core_ui.DelegateScreen
import com.wardellbagby.deltas.android.core_ui.withContentPadding
import com.wardellbagby.deltas.android.loggedin.Destination.Friends
import com.wardellbagby.deltas.android.loggedin.Destination.Settings
import com.wardellbagby.deltas.android.loggedin.Destination.Trackers
import com.wardellbagby.deltas.android.strings.TextData
import com.wardellbagby.deltas.android.strings.asTextData

data class LoggedInRendering(
  val screen: BottomNavigationRendering,
  val overlay: Overlay? = null
)

enum class Destination {
  Trackers,
  Friends,
  Settings
}

data class BottomNavigationRendering(
  val wrapped: Screen,
  val snackbarMessage: String?,
  val onSnackbarAcknowledged: () -> Unit,
  val currentDestination: Destination,
  val onDestinationChanged: (Destination) -> Unit
) : ComposeScreen, DelegateScreen {

  override val actual: Screen
    get() = wrapped

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
      if (snackbarMessage != null) {
        snackbarHostState.showSnackbar(message = snackbarMessage)
        onSnackbarAcknowledged()
      }
    }
    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      bottomBar = {
        BottomAppBar(actions = {
          NavigationBarItem(
            destination = Trackers,
            icon = Icons.Default.List,
            label = R.string.trackers.asTextData()
          )
          NavigationBarItem(
            destination = Friends,
            icon = Icons.Default.People,
            label = R.string.friends.asTextData()
          )
          NavigationBarItem(
            destination = Settings,
            icon = Icons.Default.Settings,
            label = R.string.settings.asTextData()
          )
        })
      }
    ) {
      WorkflowRendering(
        rendering = wrapped.withContentPadding(it),
        viewEnvironment = viewEnvironment
      )
    }
  }

  @Composable
  private fun RowScope.NavigationBarItem(
    destination: Destination,
    icon: ImageVector,
    label: TextData
  ) {
    NavigationBarItem(
      selected = currentDestination == destination,
      onClick = {
        onDestinationChanged(destination)
      },
      icon = {
        Icon(
          imageVector = icon,
          contentDescription = label.get()
        )
      },
      label = { Text(label.get()) }
    )
  }
}
