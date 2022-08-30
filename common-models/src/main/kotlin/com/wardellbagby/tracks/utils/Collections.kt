package com.wardellbagby.tracks.utils

fun <T> List<T>.nullIfEmpty(): List<T>? {
  return ifEmpty {
    null
  }
}

val Map<*,*>.lastIndex: Int
  get() = size - 1
