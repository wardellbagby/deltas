package com.wardellbagby.tracks.android.firebase.notifications

import com.wardellbagby.tracks.android.networking.DefaultNetworkResult
import com.wardellbagby.tracks.models.RegisterNotificationTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationService {
  @POST("register-notification-token")
  suspend fun registerNotificationToken(
    @Body request: RegisterNotificationTokenRequest
  ): DefaultNetworkResult
}