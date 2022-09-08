package com.wardellbagby.tracks.android.strings

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wardellbagby.tracks.android.strings.TextData.CompoundText
import com.wardellbagby.tracks.android.strings.TextData.ResourceText
import com.wardellbagby.tracks.android.strings.TextData.StringText
import kotlinx.parcelize.Parcelize

sealed interface TextData : Parcelable {
  @Parcelize
  data class ResourceText(@StringRes val stringRes: Int) : TextData

  @Parcelize
  data class StringText(val string: String) : TextData

  @Parcelize
  data class CompoundText(val first: TextData, val second: TextData) : TextData

  @Composable
  fun get(): String = when (this) {
    is ResourceText -> stringResource(stringRes)
    is StringText -> string
    is CompoundText -> first.get() + second.get()
  }

  fun toString(context: Context): String {
    return when (this) {
      is CompoundText -> first.toString(context) + second.toString(context)
      is ResourceText -> context.resources.getString(stringRes)
      is StringText -> string
    }
  }
}

fun Int.asTextData(): TextData = ResourceText(this)
fun String.asTextData(): TextData = StringText(this)
operator fun TextData.plus(other: TextData): TextData = CompoundText(this, other)
