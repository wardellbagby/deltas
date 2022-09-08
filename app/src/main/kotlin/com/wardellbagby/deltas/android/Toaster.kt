package com.wardellbagby.deltas.android

import android.content.Context
import android.widget.Toast
import com.wardellbagby.deltas.android.strings.TextData
import com.wardellbagby.deltas.android.strings.asTextData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * A class that can used in order to show a basic toast message.
 */
class Toaster
@Inject constructor(@ApplicationContext private val context: Context) {
  fun showToast(message: TextData) {
    Toast.makeText(context, message.asString(context), Toast.LENGTH_SHORT).show()
  }
}

fun Toaster.showToast(message: String) {
  showToast(message.asTextData())
}
