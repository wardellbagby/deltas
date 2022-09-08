package com.wardellbagby.deltas.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterNotificationTokenRequest(
  val token: String
)
