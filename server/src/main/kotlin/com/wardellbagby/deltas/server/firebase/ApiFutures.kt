package com.wardellbagby.deltas.server.firebase

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun <T> ApiFutureCallback(
  onSuccess: (T) -> Unit,
  onFailure: (Throwable) -> Unit
): ApiFutureCallback<T> {
  return object : ApiFutureCallback<T> {
    override fun onFailure(t: Throwable) {
      onFailure(t)
    }

    override fun onSuccess(result: T) {
      onSuccess(result)
    }
  }
}

suspend fun <T> ApiFuture<T>.await(): T {
  return suspendCoroutine { continuation ->
    ApiFutures.addCallback(
      this,
      ApiFutureCallback(
        onSuccess = { continuation.resume(it) },
        onFailure = { continuation.resumeWithException(it) }
      ),
      MoreExecutors.directExecutor()
    )
  }
}

suspend fun <T> ApiFuture<T>.awaitCatching(): Result<T> {
  return runCatching { await() }
}
