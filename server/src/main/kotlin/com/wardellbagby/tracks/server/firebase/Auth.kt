package com.wardellbagby.tracks.server.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord

fun FirebaseAuth.validateUIDs(
  ids: List<String>
): Boolean {
  return ids.all {
    getUser(it) != null
  }
}

val UserRecord.label: String
  get() = displayName ?: email
