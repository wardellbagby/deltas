package com.wardellbagby.tracks.android.core_ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen

object LoadingScreen: ComposeScreen {

  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Box(
      Modifier
        .fillMaxSize()
        .padding(viewEnvironment[ContentPadding]),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  }
}