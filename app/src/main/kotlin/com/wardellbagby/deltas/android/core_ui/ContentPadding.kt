package com.wardellbagby.deltas.android.core_ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewEnvironmentKey
import com.squareup.workflow1.ui.container.EnvironmentScreen

object ContentPadding : ViewEnvironmentKey<PaddingValues>(
  type = PaddingValues::class
) {
  override val default: PaddingValues
    get() = PaddingValues()
}

class CompoundPaddingValues(
  private val first: PaddingValues,
  private val second: PaddingValues
) : PaddingValues {
  override fun calculateBottomPadding(): Dp =
    first.calculateBottomPadding() + second.calculateBottomPadding()

  override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
    first.calculateLeftPadding(layoutDirection) + second.calculateLeftPadding(layoutDirection)

  override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
    first.calculateRightPadding(layoutDirection) + second.calculateRightPadding(layoutDirection)

  override fun calculateTopPadding(): Dp =
    first.calculateTopPadding() + second.calculateTopPadding()
}

class OverridingPaddingValues(
  private val delegate: PaddingValues,
  private val start: Dp? = null,
  private val top: Dp? = null,
  private val end: Dp? = null,
  private val bottom: Dp? = null
) : PaddingValues {
  private fun leftOverride(layoutDirection: LayoutDirection): Dp? {
    return when (layoutDirection) {
      Ltr -> start
      Rtl -> end
    }
  }

  private fun rightOverride(layoutDirection: LayoutDirection): Dp? {
    return when (layoutDirection) {
      Ltr -> end
      Rtl -> start
    }
  }

  override fun calculateBottomPadding(): Dp {
    return bottom ?: delegate.calculateBottomPadding()
  }

  override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
    return leftOverride(layoutDirection) ?: delegate.calculateLeftPadding(layoutDirection)
  }

  override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
    return rightOverride(layoutDirection) ?: delegate.calculateRightPadding(layoutDirection)
  }

  override fun calculateTopPadding(): Dp {
    return top ?: delegate.calculateTopPadding()
  }
}

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
  CompoundPaddingValues(first = this, second = other)

fun PaddingValues.copy(
  start: Dp? = null,
  top: Dp? = null,
  end: Dp? = null,
  bottom: Dp? = null
): PaddingValues {
  return OverridingPaddingValues(
    delegate = this,
    start = start,
    top = top,
    end = end,
    bottom = bottom
  )
}

fun Screen.withContentPadding(
  values: PaddingValues,
  environment: ViewEnvironment = ViewEnvironment.EMPTY
): Screen {
  return EnvironmentScreen(
    wrapped = this,
    environment = environment + (ContentPadding to values)
  )
}