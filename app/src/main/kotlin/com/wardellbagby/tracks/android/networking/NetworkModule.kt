package com.wardellbagby.tracks.android.networking

import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wardellbagby.tracks.android.BuildConfig
import com.wardellbagby.tracks.android.firebase.notifications.NotificationService
import com.wardellbagby.tracks.android.friends.FriendsService
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.android.trackers.TrackerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import java.time.Duration
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
  @Provides
  @Singleton
  fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
  }

  @Provides
  fun provideHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .callTimeout(Duration.ofMinutes(1))
      .connectTimeout(Duration.ofMinutes(1))
      .readTimeout(Duration.ofMinutes(1))
      .writeTimeout(Duration.ofMinutes(1))
      .addInterceptor {
        it.proceed(
          it.request().newBuilder()
            .addHeader("Tracks-Client", "Android")
            .addHeader("Tracks-Client-Version", BuildConfig.VERSION_NAME)
            .build()
        )
      }
      .addInterceptor {
        it.proceed(
          it.request().newBuilder()
            .let { request ->
              // runBlocking isn't preferred here BUT is okay because OkHttp will only block
              // this single call; others will go through as needed.
              val token = runBlocking {
                runCatching {
                  FirebaseAuth.getInstance().currentUser
                    ?.getIdToken(false)
                    ?.await()
                    ?.token
                }.getOrNull()
              }
              if (token.isNotNullOrBlank()) {
                request.addHeader("Authorization", token)
              } else {
                request
              }
            }
            .build()
        )
      }
      .build()
  }

  @OptIn(ExperimentalSerializationApi::class)
  @Provides
  fun provideRetrofit(json: Json, client: OkHttpClient): Retrofit = Retrofit.Builder()
    .baseUrl("https://services.wardell.dev/tracks/")
    .client(client)
    .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
    .addCallAdapterFactory(NetworkResultAdapterFactory.create())
    .build()

  @Provides
  fun provideTrackerService(retrofit: Retrofit): TrackerService = retrofit.create()

  @Provides
  fun provideFriendsService(retrofit: Retrofit): FriendsService = retrofit.create()

  @Provides
  fun provideNotificationService(retrofit: Retrofit): NotificationService = retrofit.create()
}