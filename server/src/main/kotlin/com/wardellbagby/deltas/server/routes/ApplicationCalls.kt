package com.wardellbagby.deltas.server.routes

import com.google.firebase.auth.UserRecord
import com.wardellbagby.deltas.server.firebase.auth
import com.wardellbagby.deltas.server.logger
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.Marker
import org.slf4j.helpers.BasicMarkerFactory

fun marker(label: String): Marker = BasicMarkerFactory().getMarker(label)

suspend inline fun <reified T : Any> ApplicationCall.safeReceive(): T? {
  return runCatching {
    receive<T>()
  }.let {
    if (it.isFailure) {
      logger.error(
        marker("safeReceive"),
        "Failed to safely convert application body to correct type."
      )
      respond(HttpStatusCode.BadRequest)
      null
    } else {
      it.getOrThrow()
    }
  }
}

suspend fun ApplicationCall.getUser(): UserRecord? {
  val authHeader = request.header("Authorization") ?: return null
  return runCatching { auth.verifyIdToken(authHeader) }
    .mapCatching { auth.getUser(it.uid) }
    .onFailure {
      logger.error(marker("getUser"), "Failed to get user!", it)
    }
    .fold(
      onSuccess = { it },
      onFailure = {
        respond(HttpStatusCode.BadRequest)
        null
      }
    )
}
