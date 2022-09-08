package com.wardellbagby.deltas.android.friends.add

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.core_ui.ContentPadding
import com.wardellbagby.deltas.android.core_ui.autofill
import com.wardellbagby.deltas.android.strings.isValidEmail

data class AddFriendScreen(
  val emailTextController: TextController,
  val onBack: () -> Unit,
  val onAddClicked: () -> Unit
) : ComposeScreen {
  @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    BackHandler {
      onBack()
    }

    var email by emailTextController.asMutableState()

    Column(
      Modifier
        .fillMaxSize()
        .padding(viewEnvironment[ContentPadding]),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(stringResource(R.string.add_new_friend), style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(16.dp))
      OutlinedTextField(
        modifier = Modifier.autofill(
          autofillTypes = listOf(AutofillType.EmailAddress),
          onFill = {
            email = it
          }),
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.friend_email_address)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
      )
      Spacer(Modifier.height(16.dp))
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onAddClicked() },
        enabled = emailTextController.textValue.isValidEmail()
      ) {
        Text(stringResource(R.string.add))
      }
    }
  }
}