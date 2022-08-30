package com.wardellbagby.tracks.android.core_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StickyHeaderText(
  text: String,
  color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
  backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
  return Text(
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = color,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .background(backgroundColor)
      .padding(vertical = 8.dp, horizontal = 16.dp)
  )
}