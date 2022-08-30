package com.wardellbagby.tracks.android.friends

import com.wardellbagby.tracks.android.networking.DefaultNetworkResult
import com.wardellbagby.tracks.android.networking.NetworkResult
import com.wardellbagby.tracks.models.friends.AddFriendRequest
import com.wardellbagby.tracks.models.friends.ListFriendsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FriendsService {
  @GET("friends/list")
  suspend fun listFriends(): NetworkResult<ListFriendsResponse>

  @POST("friends/add")
  suspend fun addFriend(@Body request: AddFriendRequest): DefaultNetworkResult
}