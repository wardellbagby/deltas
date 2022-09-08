package com.wardellbagby.deltas.models.friends

import kotlinx.serialization.Serializable

@Serializable
data class AddFriendRequest(val email: String)
