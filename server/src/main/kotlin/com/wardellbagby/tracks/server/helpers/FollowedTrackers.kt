package com.wardellbagby.tracks.server.helpers

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.SetOptions
import com.google.firebase.auth.UserRecord
import com.wardellbagby.tracks.server.firebase.awaitCatching
import com.wardellbagby.tracks.server.firebase.database

suspend fun UserRecord.getFollowedTrackers(): List<DocumentReference> {
  @Suppress("UNCHECKED_CAST")
  return database.collection("users")
    .document(uid)
    .get()
    .awaitCatching()
    .getOrNull()
    ?.get("followedTrackers") as? List<DocumentReference>
    ?: emptyList()
}

suspend fun UserRecord.addFollowedTracker(ref: DocumentReference): Result<Unit> {
  return database.collection("users")
    .document(uid)
    .set(mapOf("followedTrackers" to FieldValue.arrayUnion(ref)), SetOptions.merge())
    .awaitCatching()
    .map { }
}

suspend fun UserRecord.removeFollowedTracker(ref: DocumentReference): Result<Unit> {
  return database.collection("users")
    .document(uid)
    .set(mapOf("followedTrackers" to FieldValue.arrayRemove(ref)), SetOptions.merge())
    .awaitCatching()
    .map { }
}