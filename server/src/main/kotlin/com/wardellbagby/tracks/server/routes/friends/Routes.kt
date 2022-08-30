package com.wardellbagby.tracks.server.routes.friends

import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.friendRoutes() = route("/friends") {
  listFriends()
  deleteFriend()
  addFriend()
}
