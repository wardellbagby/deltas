package com.wardellbagby.deltas.models.trackers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TrackerVisibility {
  @SerialName("private")
  Private,

  @SerialName("public")
  Public
}