package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wardellbagby.deltas.android.strings.isNotNullOrBlank

@Composable
fun LabeledRadioButton(
  modifier: Modifier = Modifier,
  label: String,
  subLabel: String? = null,
  selected: Boolean,
  onClick: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable { onClick() }) {
    RadioButton(
      selected = selected,
      onClick = onClick
    )
    Column(modifier = Modifier.padding(4.dp)) {
      Text(label)
      if (subLabel.isNotNullOrBlank()) {
        Text(subLabel, style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}