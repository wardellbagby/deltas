package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.strings.TextData

class FailureScreen(
  val title: TextData? = null,
  val message: TextData,
  val onRetry: (() -> Unit)? = null,
  val onOk: (() -> Unit)? = null,
) : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(viewEnvironment[ContentPadding]),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      if (title != null) {
        Text(
          title.get(),
          style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
      }
      Text(message.get())
      if (onRetry != null) {
        Spacer(Modifier.height(16.dp))
        Button(
          onClick = { onRetry.invoke() },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          )
        ) {
          Text(stringResource(R.string.retry))
        }
      }
      if (onOk != null) {
        Spacer(Modifier.height(16.dp))
        Button(
          onClick = { onOk.invoke() },
        ) {
          Text(stringResource(R.string.ok))
        }
      }
    }
  }
}