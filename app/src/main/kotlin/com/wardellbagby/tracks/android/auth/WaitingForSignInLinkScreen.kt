package com.wardellbagby.tracks.android.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen

data class WaitingForSignInLinkScreen(
  val onTryAgain: () -> Unit
) : ComposeScreen {

  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Column(
      Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        "Check your email",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(16.dp))

      Text(
        "Open your email on your phone for a link to automatically sign-in",
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(16.dp))

      Text(
        "The email can take a few minutes to appear. Don't forget to check your spam folder!",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(48.dp))

      OutlinedButton(
        onClick = {
          onTryAgain()
        }) {
        Text("I don't see an email?")
      }
    }
  }
}
