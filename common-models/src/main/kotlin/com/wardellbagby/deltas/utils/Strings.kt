package com.wardellbagby.deltas.utils

fun String.nullIfBlank(): String? = ifBlank { null }
