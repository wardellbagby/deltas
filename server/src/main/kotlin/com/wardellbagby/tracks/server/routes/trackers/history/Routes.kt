package com.wardellbagby.tracks.server.routes.trackers.history

import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.historyRoutes() = route("/history") {
  listHistory()
}
