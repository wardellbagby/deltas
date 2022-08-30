package com.wardellbagby.tracks.android.friends

import android.os.Parcelable
import com.wardellbagby.tracks.models.friends.FriendDTO
import kotlinx.parcelize.Parcelize

@Parcelize
data class Friend(
  val id: String,
  val label: String,
) : Parcelable

fun FriendDTO.toModel() = Friend(id = id, label = label)
fun List<FriendDTO>.toModels() = map { it.toModel() }

