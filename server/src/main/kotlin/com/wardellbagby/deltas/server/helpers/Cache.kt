package com.wardellbagby.deltas.server.helpers

import kotlin.collections.MutableMap.MutableEntry

open class Cache<K, V>(
  private val maxCapacity: Int = DEFAULT_MAX_CAPACITY,
  protected var onValueAdded: Cache<K, V>.(K, V) -> Unit = { _, _ -> },
  protected var onValueChanged: Cache<K, V>.(K, V) -> Unit = { _, _ -> },
  protected var onValueRemoved: Cache<K, V>.(K, V) -> Unit = { _, _ -> },
  protected var onClear: Cache<K, V>.() -> Unit = {},
) : LinkedHashMap<K, V>() {

  private companion object {
    private const val DEFAULT_MAX_CAPACITY = 1_000
  }

  override fun put(key: K, value: V): V? {
    val hasKey = contains(key)
    return super.put(key, value).also {
      if (hasKey) {
        onValueChanged(key, value)
      } else {
        onValueAdded(key, value)
      }
    }
  }

  override fun remove(key: K): V? {
    return super.remove(key)
      .also {
        if (it != null) {
          onValueRemoved(key, it)
        }
      }
  }

  override fun removeEldestEntry(eldest: MutableEntry<K, V>): Boolean {
    return (size > maxCapacity).also {
      if (it) {
        onValueRemoved(eldest.key, eldest.value)
      }
    }
  }

  override fun clear() {
    super.clear()
    onClear()
  }
}