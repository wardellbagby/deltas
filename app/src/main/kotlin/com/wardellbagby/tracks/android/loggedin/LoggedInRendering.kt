package com.wardellbagby.tracks.android.loggedin

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.container.Overlay
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.withContentPadding
import com.wardellbagby.tracks.android.loggedin.Destination.Friends
import com.wardellbagby.tracks.android.loggedin.Destination.Settings
import com.wardellbagby.tracks.android.loggedin.Destination.Trackers
import com.wardellbagby.tracks.android.strings.TextData
import com.wardellbagby.tracks.android.strings.asTextData

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
  val currentDestination: Destination,
  val onDestinationChanged: (Destination) -> Unit
) : ComposeScreen {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Scaffold(
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
