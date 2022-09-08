package com.wardellbagby.deltas.android.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.strings.isValidEmail

@OptIn(ExperimentalMaterial3Api::class)
data class SignInScreen(
  val emailTextController: TextController,
  val onSignIn: () -> Unit,
  val onEmailPassword: () -> Unit,
) : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    var email by emailTextController.asMutableState()

    val areButtonsEnabled = remember(email) {
      email.isValidEmail()
    }

    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.displayMedium
      )

      Spacer(modifier = Modifier.height(32.dp))

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.email)) },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))


      Button(
        onClick = { onSignIn() },
        enabled = areButtonsEnabled,
      ) {
        Text(stringResource(R.string.sign_in_or_create_account))
      }

      Spacer(modifier = Modifier.weight(1f))

      OutlinedButton(
        onClick = { onEmailPassword() }
      ) {
        Text(stringResource(R.string.use_email_and_password))
      }
    }
  }
}