package com.wardellbagby.deltas.android.core_ui

import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.container.BodyAndOverlaysScreen

/**
 * A marker interface for screens who don't primarily show their own content but instead show the
 * content of another screen.
 */
interface DelegateScreen {
  val actual: Screen
}

fun Screen.getActual(): Screen {
  return when (this) {
    is BodyAndOverlaysScreen<*, *> -> body.getActual()
    is DelegateScreen -> actual.getActual()
    else -> this
  }
}