package com.wardellbagby.tracks.server.firebase

import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.FirebaseMessaging

val auth: FirebaseAuth = FirebaseAuth.getInstance()
val database: Firestore = FirestoreClient.getFirestore()
val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
