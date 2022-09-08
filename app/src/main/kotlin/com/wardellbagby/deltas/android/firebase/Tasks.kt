package com.wardellbagby.deltas.android.firebase

import com.google.android.gms.tasks.Task
import com.squareup.workflow1.Worker
import kotlinx.coroutines.tasks.await

inline fun <reified T> Task<T>.asWorker(): Worker<Result<T>> {
  return Worker.create {
    emit(runCatching { this@asWorker.await() })
  }
}