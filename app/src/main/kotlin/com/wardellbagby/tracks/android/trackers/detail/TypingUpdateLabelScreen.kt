package com.wardellbagby.tracks.android.trackers.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.models.trackers.TrackerType
import com.wardellbagby.tracks.models.trackers.TrackerType.Elapsed
import com.wardellbagby.tracks.models.trackers.TrackerType.Incremental

// Yeah, I tweet a lot so...
private const val MAX_LABEL_LENGTH = 240

data class TypingUpdateLabelScreen(
  val labelTextController: TextController,
  val type: TrackerType,
  val onUpdateClicked: () -> Unit,
  val onBack: () -> Unit
) : ComposeScreen {

  private fun TrackerType.toLabel() = when (this) {
    Elapsed -> "Reset time"
    Incremental -> "Increment"
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    BackHandler {
      onBack()
    }

    var label by labelTextController.asMutableState()

    Column(
      Modifier
        .fillMaxSize()
        .padding(viewEnvironment[ContentPadding]),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(stringResource(R.string.update_tracker), style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(16.dp))
      OutlinedTextField(
        value = label,
        onValueChange = { label = it.take(MAX_LABEL_LENGTH) },
        label = { Text(stringResource(R.string.optioanl_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
      )
      Spacer(Modifier.height(16.dp))
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onUpdateClicked() },
      ) {
        Text(type.toLabel())
      }
    }
  }
}