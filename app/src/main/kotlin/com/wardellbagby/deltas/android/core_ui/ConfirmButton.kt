package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wardellbagby.deltas.android.strings.TextData
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ConfirmButton(
  label: TextData,
  confirmLabel: TextData,
  onClick: () -> Unit
) {
  val modifier = Modifier.fillMaxWidth()
  var isConfirming by remember { mutableStateOf(false) }

  LaunchedEffect(isConfirming) {
    if (isConfirming) {
      delay(3.seconds)
      isConfirming = false
    }
  }

  if (isConfirming) {
    Button(
      modifier = modifier,
      onClick = { onClick() },
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
      )
    ) {
      Text(confirmLabel.get())
    }
  } else {
    Button(
      modifier = modifier,
      onClick = { isConfirming = true },
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
      )
    ) {
      Text(label.get())
    }
  }
}