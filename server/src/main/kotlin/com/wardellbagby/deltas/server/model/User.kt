package com.wardellbagby.deltas.server.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val messageToken: String
)
