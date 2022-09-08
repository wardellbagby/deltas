package com.wardellbagby.deltas.server.firebase

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

data class ValueWithId<T>(
  val id: String,
  val value: T
)

suspend inline fun <reified T> DocumentReference.setCatching(
  value: T,
  options: SetOptions? = null
): Result<WriteResult> {
  when (value) {
    is Boolean, is String, is Number ->
      Result.failure<Nothing>(Exception("Cannot add Java primitives directly to a document."))
  }
  return runCatching {
    val databaseValue = Json.encodeToJsonElement(value).asPlainType()

    if (databaseValue !is Map<*, *>) {
      return Result.failure(Exception("Cannot add Java primitives directly to a document."))
    }

    @Suppress("UNCHECKED_CAST")
    if (options != null) {
      set(databaseValue as Map<String, Any?>, options).await()
    } else {
      set(databaseValue as Map<String, Any?>).await()
    }
  }
}

suspend inline fun <reified T> CollectionReference.push(value: T): Result<Unit> {
  when (value) {
    is Boolean, is String, is Number -> Result.failure<Nothing>(
      Exception("Cannot add Java primitives directly to a collection.")
    )
  }
  return runCatching {
    val databaseValue = Json.encodeToJsonElement(value).asPlainType()

    if (databaseValue !is Map<*, *>) {
      return Result.failure(Exception("Cannot add Java primitives directly to a collection."))
    }

    @Suppress("UNCHECKED_CAST")
    add(databaseValue as Map<String, Any?>).await()
  }
}

suspend inline fun <reified T> DocumentReference.getOrNull(): Result<ValueWithId<T>?> {
  val result = get().await()
  if (!result.exists()) {
    return Result.success(null)
  }

  val data = result.data!!
  val element = data.toJsonElement()
  return runCatching {
    ValueWithId(id = result.id, value = Json.decodeFromJsonElement(element))
  }
}

suspend inline fun <reified T> Query.getOrEmpty(): Result<List<ValueWithId<T>>> {
  val result = get().await()
  if (result.isEmpty) {
    return Result.success(emptyList())
  }

  return runCatching { result.documents }
    .mapCatching {
      it.map { ref -> ref to ref.data.toJsonElement() }
        .map { (ref, element) ->
          ValueWithId(
            id = ref.id,
            value = Json.decodeFromJsonElement(element)
          )
        }
    }
}

fun Collection<*>.toJsonElement() = JsonArray(mapNotNull { it.toJsonElement() })

fun Map<*, *>.toJsonElement(): JsonElement = JsonObject(
  mapNotNull {
    (it.key as? String ?: return@mapNotNull null) to it.value.toJsonElement()
  }.toMap(),
)

fun Any?.toJsonElement(): JsonElement = when (this) {
  null -> JsonNull
  is Map<*, *> -> toJsonElement()
  is Collection<*> -> toJsonElement()
  is String -> JsonPrimitive(this)
  is Boolean -> JsonPrimitive(this)
  is Number -> JsonPrimitive(this)
  else -> error("Unknown value: $this")
}

/**
 * Convert a [JsonElement] to a "plain" type.
 *
 * JsonObject -> Map<String, PlainType>
 * JsonArray -> List<PlainType>
 * JsonPrimitive -> actual primitive
 */
fun JsonElement.asPlainType(): Any? = when (this) {
  is JsonNull -> null
  is JsonArray -> map { it.asPlainType() }
  is JsonObject -> entries.associate { (key, value) -> key to value.asPlainType() }
  is JsonPrimitive -> {
    booleanOrNull ?: intOrNull ?: floatOrNull ?: longOrNull
      ?: doubleOrNull ?: content
  }
}
