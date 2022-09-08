package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DataRow(modifier: Modifier = Modifier, label: String, value: String) {
  Row(
    modifier = modifier
      .padding(vertical = 16.dp)
      .defaultMinSize(minHeight = 64.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      label,
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.weight(1f)
    )
    Spacer(Modifier.width(8.dp))
    Text(
      value,
      textAlign = TextAlign.Right,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f)
    )
  }
}