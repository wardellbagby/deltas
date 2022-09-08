package com.wardellbagby.tracks.android.trackers

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.android.core_ui.DelegateScreen
import com.wardellbagby.tracks.android.strings.TextData

interface ContributesToTopBar {
  val actions: @Composable RowScope.() -> Unit
    get() = {}
}

interface HasPageTitle {
  val title: TextData
}

data class TrackersChildScreen(
  val title: TextData,
  val rendering: Screen,
  val onBack: () -> Unit,
) : ComposeScreen, DelegateScreen {
  override val actual: Screen
    get() = rendering

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    BackHandler {
      onBack()
    }

    val currentBackDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(
      modifier = Modifier.padding(viewEnvironment[ContentPadding]),
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            Text(
              title.get(),
              softWrap = false,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            )
          },
          navigationIcon = {
            IconButton(
              onClick = {
                currentBackDispatcher?.onBackPressed() ?: onBack()
              }) {
              Icon(Icons.Default.ArrowBack, "Go Back")
            }
          },
          actions = {
            if (rendering is ContributesToTopBar) {
              rendering.actions(this)
            }

          }
        )
      },
      content = {
        WorkflowRendering(
          modifier = Modifier.padding(it),
          rendering = rendering,
          viewEnvironment = viewEnvironment
        )
      }
    )
  }
}