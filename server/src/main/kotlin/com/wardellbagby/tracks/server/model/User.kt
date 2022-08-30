package com.wardellbagby.tracks.server.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val messageToken: String
)
