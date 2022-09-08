package com.wardellbagby.deltas.models.trackers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TrackerType {
  @SerialName("elapsed")
  Elapsed,

  @SerialName("incremental")
  Incremental
}
