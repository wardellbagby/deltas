package com.wardellbagby.deltas.models

import kotlinx.serialization.Serializable

interface ServerResponse {
  val success: Boolean
  val errorDetailMessage: String?
}

@Serializable
data class DefaultServerResponse(
  override val success: Boolean = true,
  override val errorDetailMessage: String? = null
) : ServerResponse
