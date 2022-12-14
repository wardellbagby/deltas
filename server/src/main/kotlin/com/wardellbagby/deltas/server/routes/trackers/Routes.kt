package com.wardellbagby.deltas.server.routes.trackers

import com.wardellbagby.deltas.server.routes.trackers.history.historyRoutes
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.trackerRoutes() = route("/tracker") {
  createTracker()
  getTracker()
  listTrackers()
  updateTrackers()
  deleteTracker()
  subscribeTracker()
  unsubscribeTracker()

  historyRoutes()
}
