package com.wardellbagby.tracks.server.helpers

inline fun <T, R> Result<T>.flatMap(block: (T) -> Result<R>): Result<R> {
  return if (isSuccess) {
    block(getOrThrow())
  } else {
    Result.failure(exceptionOrNull()!!)
  }
}

fun <T> List<Result<T>>.combine(): Result<List<T>> {
  return runCatching {
    map { it.getOrThrow() }
  }
}

fun <T> Result<T?>.failIfNull(): Result<T> {
  return flatMap {
    if (it == null) {
      Result.failure(Exception("Value was null"))
    } else {
      Result.success(it)
    }
  }
}
