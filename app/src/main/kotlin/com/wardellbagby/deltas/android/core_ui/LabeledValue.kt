package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LabeledValue(label: String, value: String) {
  Column {
    Text(
      label,
      style = MaterialTheme.typography.labelMedium
    )
    Text(value)
  }
}