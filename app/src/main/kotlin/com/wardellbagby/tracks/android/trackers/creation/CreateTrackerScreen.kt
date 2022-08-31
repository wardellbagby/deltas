package com.wardellbagby.tracks.android.trackers.creation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.LabeledRadioButton
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.models.trackers.TrackerType
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental

data class CreateTrackerScreen(
  val type: TrackerType,
  val labelTextController: TextController,
  val onTypeSelected: (type: TrackerType) -> Unit,
  val onBack: () -> Unit,
  val onSave: () -> Unit
) : ComposeScreen {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    var label by labelTextController.asMutableState()
    val focusRequester = remember {
      FocusRequester()
    }
    val focusManager = LocalFocusManager.current

    BackHandler {
      focusManager.clearFocus(force = true)
      onBack()
    }

    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }

    Column(
      Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        label = { Text(stringResource(R.string.label)) },
        value = label,
        onValueChange = { label = it }
      )

      Spacer(modifier = Modifier.height(32.dp))

      Column(Modifier.fillMaxWidth()) {
        Text(
          stringResource(R.string.what_should_this_track),
          style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LabeledRadioButton(
          modifier = Modifier.fillMaxWidth(),
          label = stringResource(R.string.time_since_reset),
          selected = type == Elapsed,
          onClick = { onTypeSelected(Elapsed) })
        LabeledRadioButton(
          modifier = Modifier.fillMaxWidth(),
          label = stringResource(R.string.total_action_count),
          selected = type == Incremental,
          onClick = { onTypeSelected(Incremental) })
      }

      Spacer(modifier = Modifier.weight(1f))

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = label.isNotNullOrBlank(),
        onClick = {
          focusManager.clearFocus(force = true)
          onSave()
        }) {
        Text(text = stringResource(R.string.save))
      }
    }
  }
}