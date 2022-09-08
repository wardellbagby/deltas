package com.wardellbagby.deltas.android.friends

import com.wardellbagby.deltas.android.networking.DefaultNetworkResult
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.models.friends.AddFriendRequest
import com.wardellbagby.deltas.models.friends.ListFriendsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FriendsService {
  @GET("friends/list")
  suspend fun listFriends(): NetworkResult<ListFriendsResponse>

  @POST("friends/add")
  suspend fun addFriend(@Body request: AddFriendRequest): DefaultNetworkResult
}