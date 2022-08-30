package com.wardellbagby.tracks.android.strings

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun String?.isNotNullOrBlank(): Boolean {
  contract {
    returns(true) implies (this@isNotNullOrBlank != null)
  }

  return !isNullOrBlank()
}

fun String?.isValidEmail() = isNotNullOrBlank() && matches(Regex(".+@.+"))