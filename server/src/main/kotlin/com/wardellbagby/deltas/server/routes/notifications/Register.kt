package com.wardellbagby.deltas.server.routes.notifications

import com.google.cloud.firestore.SetOptions
import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.RegisterNotificationTokenRequest
import com.wardellbagby.deltas.server.firebase.awaitCatching
import com.wardellbagby.deltas.server.firebase.database
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.registerNotifications() = post("register-notification-token") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<RegisterNotificationTokenRequest>() ?: return@post

  if (body.token.isBlank()) {
    call.respond(DefaultServerResponse(success = false))
    return@post
  }
  database.collection("users")
    .document(user.uid)
    .set(mapOf("messageToken" to body.token), SetOptions.merge())
    .awaitCatching()
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
