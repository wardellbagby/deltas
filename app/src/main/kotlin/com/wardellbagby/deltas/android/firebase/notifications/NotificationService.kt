package com.wardellbagby.deltas.android.firebase.notifications

import com.wardellbagby.deltas.android.networking.DefaultNetworkResult
import com.wardellbagby.deltas.models.RegisterNotificationTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationService {
  @POST("register-notification-token")
  suspend fun registerNotificationToken(
    @Body request: RegisterNotificationTokenRequest
  ): DefaultNetworkResult
}