package com.wardellbagby.tracks.android.settings

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

private const val MAX_DISPLAY_NAME_LENGTH = 100
private const val MIN_DISPLAY_NAME_LENGTH = 5

data class EnteringDisplayNameScreen(
  val originalDisplayName: String,
  val displayNameTextController: TextController,
  val onBack: () -> Unit,
  val onSaveClicked: () -> Unit
) : ComposeScreen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    BackHandler {
      onBack()
    }

    var displayName by displayNameTextController.asMutableState()

    Column(
      Modifier
        .fillMaxSize()
        .padding(viewEnvironment[ContentPadding]),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(stringResource(R.string.display_name), style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(16.dp))
      OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it.take(MAX_DISPLAY_NAME_LENGTH) },
        label = { Text(stringResource(R.string.your_display_name)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
      )
      Spacer(Modifier.height(16.dp))
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSaveClicked() },
        enabled = displayName != originalDisplayName && displayName.length >= MIN_DISPLAY_NAME_LENGTH
      ) {
        Text(stringResource(R.string.save))
      }
    }
  }
}