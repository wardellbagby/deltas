package com.wardellbagby.tracks.android.networking

import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints
import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints.Local
import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints.Production
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Endpoint
@Inject constructor() {
  enum class Endpoints {
    Production,
    Local
  }

  var current: Endpoints
    get() = endpointChanges.value
    set(value) {
      endpointChanges.value = value
    }

  private val endpointChanges = MutableStateFlow(Production)

  val changes: Flow<Endpoints> = endpointChanges.drop(1)
}

val Endpoints.asBaseUrl: String
  get() = when (this) {
    Production -> "https://services.wardell.dev/tracks/"
    Local -> "http://10.0.2.2:21233/"
  }