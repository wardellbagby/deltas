package com.wardellbagby.tracks.android.trackers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.android.core_ui.plus
import com.wardellbagby.tracks.android.core_ui.withContentPadding

data class TrackersScreen(
  val list: Screen,
  val onCreateClicked: () -> Unit
) : ComposeScreen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Scaffold(
      modifier = Modifier.padding(viewEnvironment[ContentPadding]),
      topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.trackers)) })
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { onCreateClicked() }) {
          Text(stringResource(R.string.create_new_tracker))
        }
      },
      floatingActionButtonPosition = FabPosition.Center,
      content = {
        WorkflowRendering(
          rendering = list.withContentPadding(it + PaddingValues(bottom = 100.dp)),
          viewEnvironment = viewEnvironment
        )
      }
    )
  }
}