package com.wardellbagby.deltas.server.helpers

import com.google.cloud.firestore.DocumentReference
import com.wardellbagby.deltas.server.firebase.ValueWithId
import com.wardellbagby.deltas.server.firebase.changes
import com.wardellbagby.deltas.server.firebase.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

class FirebaseAutoUpdatingCache<T : Any>(
  private val valueSerializer: KSerializer<T>,
  scope: CoroutineScope,
  getReferenceFromKey: (String) -> DocumentReference
) : Cache<String, ValueWithId<T>>() {

  private val jobs = mutableMapOf<String, Job>()

  init {
    onValueAdded = { key, _ ->
      scope
        .launch {
          getReferenceFromKey(key)
            .changes()
            .collectLatest {
              it.flatMap { snapshot ->
                snapshot.getOrNull(valueSerializer)
              }
                .failIfNull()
                .onSuccess { value ->
                  this@FirebaseAutoUpdatingCache[key] = value
                }
                .onFailure {
                  this@FirebaseAutoUpdatingCache.remove(key)
                }
            }
        }
        .also { jobs[key] = it }
    }

    onValueRemoved = { key, _ ->
      jobs[key]?.cancel("Value has been removed.")
    }

    onClear = {
      jobs.keys
        .forEach {
          jobs[it]?.cancel("Value has been removed")
        }
    }
  }
}