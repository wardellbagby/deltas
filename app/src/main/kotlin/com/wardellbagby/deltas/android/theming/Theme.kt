package com.wardellbagby.deltas.android.theming

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * The root theme used by Workflow screens that render using Compose.
 *
 * Will use dynamic colors on Android S and higher, and falls back to the Material3 default colors
 * when dynamic color aren't available.
 */
@Composable
private fun AppTheme(
  useDarkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val colorScheme = when {
    dynamicColor && useDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
    dynamicColor && !useDarkTheme -> dynamicLightColorScheme(LocalContext.current)
    useDarkTheme -> darkColorScheme()
    else -> lightColorScheme()
  }

  MaterialTheme(colorScheme = colorScheme) {
    content()
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AppCompositionRoot(content: @Composable () -> Unit) {
  AppTheme {
    Scaffold {
      content()
    }
  }
}

@Composable
fun OverlayCompositionRoot(content: @Composable () -> Unit) {
  AppTheme {
    Box(contentAlignment = Alignment.Center) {
      Surface(
        shape = AbsoluteRoundedCornerShape(12.dp),
        modifier = Modifier
          .height(Min)
          .width(Min)
      ) {
        content()
      }
    }
  }
}
