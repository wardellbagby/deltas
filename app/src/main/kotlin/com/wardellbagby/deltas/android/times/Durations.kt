package com.wardellbagby.deltas.android.times

import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.strings.TextData
import com.wardellbagby.deltas.android.strings.asTextData
import com.wardellbagby.deltas.android.strings.plus
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.HOURS
import kotlin.time.DurationUnit.MINUTES

private fun Double.format(unit: DurationUnit): TextData {
  val formattedTime = if (this < 0.99) 0.0 else this

  val formattedUnit = when (unit) {
    MINUTES -> if (this == 1.0) R.string.minute else R.string.minutes
    HOURS -> if (this == 1.0) R.string.hour else R.string.hours
    DAYS -> if (this == 1.0) R.string.day else R.string.days
    else -> error("Cannot format $unit duration unit.")
  }.asTextData()
  return "%,.1f ".format(Locale.getDefault(), formattedTime).asTextData() + formattedUnit
}

fun Duration.format(): TextData {
  return when {
    inWholeDays > 1 -> toDouble(DAYS).format(DAYS)
    inWholeHours > 1 -> toDouble(HOURS).format(HOURS)
    else -> toDouble(MINUTES).format(MINUTES)
  }
}