import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.gradle.plugin.use.PluginDependenciesSpec

// Android Studio doesn't know that changes in this file changes dependencies, so make sure to do
// a manual Gradle sync after changing something here!
object Dependencies {
  const val kotlinComposeCompilerVersion = "1.3.0"

  // Kotlin
  const val kotlinVersion = "1.7.10"
  private const val kotlinCoroutinesVersion = "1.6.4"
  private const val kotlinDateTimeVersion = "0.4.0"
  private const val kotlinSerializationVersion = "1.4.0"

  // Gradle Plugins
  private const val ktlintGradleVersion = "10.2.1"
  private const val agpVersion = "7.2.2"
  private const val hiltVersion = "2.43.1"

  // AndroidX
  private const val recyclerViewVersion = "1.2.1"
  private const val appcompatVersion = "1.5.0"
  private const val activityKtxVersion = "1.5.1"
  private const val materialComposeVersion = "1.0.0-alpha16"
  private const val materialIconsVersion = "1.2.1"
  private const val lifecycleViewModelVersion = "2.5.1"
  private const val constraintLayoutVersion = "2.1.4"
  private const val pagingVersion = "3.1.1"
  private const val pagingComposeVersion = "1.0.0-alpha16"

  // Square
  private const val workflowVersion = "1.8.0-beta09"
  private const val retrofitVersion = "2.9.0"
  private const val serializationRetrofitAdapterVersion = "0.8.0"

  // Google Services
  private const val googleServicesVersion = "4.3.13"
  private const val googlePlayServicesAuthVersion = "20.2.0"
  private const val ktxPlayServices = "1.6.4"

  // Firebase
  private const val firebaseVersion = "30.3.2"
  private const val firebaseAdminVersion = "9.0.0"

  // Ktor
  const val ktorServerVersion = "2.1.0"

  const val koinVersion = "3.2.1"

  const val AndroidGradlePlugin = "com.android.tools.build:gradle:$agpVersion"
  const val KotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
  const val KtlintGradlePlugin = "org.jlleitschuh.gradle:ktlint-gradle:$ktlintGradleVersion"

  const val KotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
  const val KotlinDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDateTimeVersion"

  const val HiltCompiler = "com.google.dagger:hilt-compiler:$hiltVersion"
  const val HiltAndroid = "com.google.dagger:hilt-android:$hiltVersion"
  const val HiltGradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:$hiltVersion"

  const val Koin = "io.insert-koin:koin-core:$koinVersion"

  const val RecyclerView = "androidx.recyclerview:recyclerview:$recyclerViewVersion"
  const val Appcompat = "androidx.appcompat:appcompat:$appcompatVersion"
  const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
  const val LifecycleViewModel =
    "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleViewModelVersion"
  const val ActivityKtx = "androidx.activity:activity-ktx:$activityKtxVersion"
  const val ActivityCompose = "androidx.activity:activity-compose:$activityKtxVersion"
  const val MaterialCompose = "androidx.compose.material3:material3:$materialComposeVersion"
  const val MaterialIconsExtended =
    "androidx.compose.material:material-icons-extended:$materialIconsVersion"

  const val WorkflowUiCoreAndroid =
    "com.squareup.workflow1:workflow-ui-core-android:$workflowVersion"
  const val WorkflowTracing = "com.squareup.workflow1:workflow-tracing:$workflowVersion"
  const val WorkflowUiCompose = "com.squareup.workflow1:workflow-ui-compose:$workflowVersion"

  const val Retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
  const val KotlinSerialization =
    "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion"
  const val KotlinSerializationRetrofitAdapter =
    "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:$serializationRetrofitAdapterVersion"

  const val GoogleServices = "com.google.gms:google-services:$googleServicesVersion"
  const val GooglePlayServiceAuth =
    "com.google.android.gms:play-services-auth:$googlePlayServicesAuthVersion"
  const val GooglePlayServiceKtx =
    "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$ktxPlayServices"

  const val FirebaseBOM = "com.google.firebase:firebase-bom:$firebaseVersion"
  const val FirebaseAdmin = "com.google.firebase:firebase-admin:$firebaseAdminVersion"
  const val FirebaseAuth = "com.google.firebase:firebase-auth"
  const val FirebaseAuthKtx = "com.google.firebase:firebase-auth-ktx"
  const val FirebaseMessaging = "com.google.firebase:firebase-messaging"
  const val FirebaseLinks = "com.google.firebase:firebase-dynamic-links-ktx"

  const val KtorServerCore = "io.ktor:ktor-server-core-jvm:$ktorServerVersion"
  const val KtorServerCIO = "io.ktor:ktor-server-cio-jvm:$ktorServerVersion"
  const val KtorContentNegotion = "io.ktor:ktor-server-content-negotiation:$ktorServerVersion"
  const val KtorSerializationJson = "io.ktor:ktor-serialization-kotlinx-json:$ktorServerVersion"
  const val KtorLogging = "io.ktor:ktor-server-call-logging:$ktorServerVersion"
  const val KoinKtor = "io.insert-koin:koin-ktor:$koinVersion"
  const val LogbackClassic = "ch.qos.logback:logback-classic:1.2.11"
}

fun PluginDependenciesSpec.kotlinSerialization() =
  kotlin("plugin.serialization") version Dependencies.kotlinVersion

fun PluginDependenciesSpec.kotlinJvm() = kotlin("jvm")
fun PluginDependenciesSpec.ktor() = id("io.ktor.plugin") version Dependencies.ktorServerVersion