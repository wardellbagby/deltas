package com.wardellbagby.tracks.android.core_ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
  CompoundPaddingValues(first = this, second = other)

fun Screen.withContentPadding(
  values: PaddingValues,
  environment: ViewEnvironment = ViewEnvironment.EMPTY
): Screen {
  return EnvironmentScreen(
    wrapped = this,
    environment = environment + (ContentPadding to values)
  )
}