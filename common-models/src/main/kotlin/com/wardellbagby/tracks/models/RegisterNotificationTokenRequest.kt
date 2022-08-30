package com.wardellbagby.tracks.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterNotificationTokenRequest(
  val token: String
)
