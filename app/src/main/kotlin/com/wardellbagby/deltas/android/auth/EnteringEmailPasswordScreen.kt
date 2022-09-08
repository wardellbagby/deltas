package com.wardellbagby.deltas.android.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.strings.isNotNullOrBlank
import com.wardellbagby.deltas.android.strings.isValidEmail

@OptIn(ExperimentalMaterial3Api::class)
data class EnteringEmailPasswordScreen(
  val emailTextController: TextController,
  val passwordTextController: TextController,
  val onSignIn: () -> Unit,
  val onCreateAccount: () -> Unit,
  val onUseEmailLink: () -> Unit
) : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    var email by emailTextController.asMutableState()
    var password by passwordTextController.asMutableState()
    var isShowingPassword by remember { mutableStateOf(false) }

    val areButtonsEnabled = remember(email, password) {
      email.isValidEmail() && password.isNotNullOrBlank()
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
          autoCorrect = false,
          keyboardType = KeyboardType.Email,
        ),
        singleLine = true
      )

      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        visualTransformation = if (isShowingPassword) {
          VisualTransformation.None
        } else {
          PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
          autoCorrect = false,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = {
          if (areButtonsEnabled) {
            onSignIn()
          }
        }),
        trailingIcon = {
          IconButton(onClick = { isShowingPassword = !isShowingPassword }) {
            Icon(
              if (isShowingPassword) {
                Icons.Default.VisibilityOff
              } else {
                Icons.Default.Visibility
              },
              contentDescription = stringResource(R.string.show_password_content_description)
            )
          }
        },
        label = { Text(stringResource(R.string.password)) }
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(
          onClick = { onSignIn() },
          enabled = areButtonsEnabled,
        ) {
          Text(stringResource(R.string.sign_in))
        }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
          onClick = { onCreateAccount() },
          enabled = areButtonsEnabled
        ) {
          Text(stringResource(R.string.create_account))
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      OutlinedButton(
        onClick = { onUseEmailLink() }
      ) {
        Text(stringResource(R.string.use_email_link))
      }
    }
  }
}