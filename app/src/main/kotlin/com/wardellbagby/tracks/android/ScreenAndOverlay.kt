package com.wardellbagby.tracks.android

import com.squareup.workflow1.ui.Compatible
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.container.Overlay

data class ScreenAndOverlay(val screen: Screen, val overlay: Overlay? = null) : Compatible {
  override val compatibilityKey: String
    get() = "${screen::class}${overlay?.let { it::class } ?: ""}"
}

fun Screen.asScreenAndOverlay(overlay: Overlay? = null) = ScreenAndOverlay(this, overlay)