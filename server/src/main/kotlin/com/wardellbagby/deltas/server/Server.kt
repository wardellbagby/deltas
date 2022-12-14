package com.wardellbagby.deltas.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.wardellbagby.deltas.server.routes.friends.friendRoutes
import com.wardellbagby.deltas.server.routes.notifications.notificationRoutes
import com.wardellbagby.deltas.server.routes.trackers.trackerRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import java.nio.file.Path
import kotlin.io.path.inputStream

@OptIn(ExperimentalSerializationApi::class) fun main() {
  val credentialsStream = System.getenv("TRACKS_SERVER_GOOGLE_APPLICATION_CREDENTIALS")
    ?.let { Path.of(it) }
    ?.inputStream()
    ?: error("Must set a valid path to a Google Application Credentials JSON file")
  val port = System.getenv("TRACKS_SERVER_PORT").toIntOrNull()
    ?: error("Must set an int port using the env TRACKS_SERVER_PORT")

  FirebaseApp.initializeApp(
    FirebaseOptions.builder()
      .setCredentials(GoogleCredentials.fromStream(credentialsStream))
      .build()
  )

  embeddedServer(CIO, port = port) {
    install(Koin) {
      modules(createMainModule(CoroutineScope(coroutineContext)))
    }
    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          explicitNulls = false
        }
      )
    }
    install(CallLogging)

    routing {
      trackerRoutes()
      friendRoutes()
      notificationRoutes()
    }
  }.start(wait = true)
}
