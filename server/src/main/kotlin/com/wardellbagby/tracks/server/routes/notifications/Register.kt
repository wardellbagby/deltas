package com.wardellbagby.tracks.server.routes.notifications

import com.google.cloud.firestore.FieldPath
import com.google.cloud.firestore.SetOptions
import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.RegisterNotificationTokenRequest
import com.wardellbagby.tracks.server.firebase.database
import com.wardellbagby.tracks.server.firebase.setCatching
import com.wardellbagby.tracks.server.model.User
import com.wardellbagby.tracks.server.routes.getUser
import com.wardellbagby.tracks.server.routes.safeReceive
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
    .setCatching(
      User(messageToken = body.token),
      SetOptions.mergeFieldPaths(listOf(FieldPath.of("messageToken")))
    )
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
