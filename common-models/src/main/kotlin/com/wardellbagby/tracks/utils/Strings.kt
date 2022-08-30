package com.wardellbagby.tracks.utils

fun String.nullIfBlank(): String? = ifBlank { null }
