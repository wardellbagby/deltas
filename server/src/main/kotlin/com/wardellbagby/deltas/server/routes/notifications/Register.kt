package com.wardellbagby.deltas.server.routes.notifications

import com.wardellbagby.deltas.models.DefaultServerResponse
import com.wardellbagby.deltas.models.RegisterNotificationTokenRequest
import com.wardellbagby.deltas.server.routes.getUser
import com.wardellbagby.deltas.server.routes.safeReceive
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import com.wardellbagby.deltas.server.routes.users.updateUserData
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.registerNotifications() = post("register-notification-token") {
  val user = call.getUser() ?: return@post
  val body = call.safeReceive<RegisterNotificationTokenRequest>() ?: return@post

  val repository: UserDataRepository by this@registerNotifications.inject()

  if (body.token.isBlank()) {
    call.respond(DefaultServerResponse(success = false))
    return@post
  }

  repository.updateUserData(user) {
    copy(messageToken = body.token)
  }
    .fold(
      onSuccess = { call.respond(DefaultServerResponse()) },
      onFailure = { call.respond(DefaultServerResponse(success = false)) }
    )
}
