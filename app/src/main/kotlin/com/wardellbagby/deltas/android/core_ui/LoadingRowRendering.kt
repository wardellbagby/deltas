package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen

object LoadingRowRendering : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Box(
      modifier = Modifier.fillMaxWidth().height(72.dp).padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  }
}
