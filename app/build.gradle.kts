import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-kapt")
  id("kotlin-parcelize")
  kotlinSerialization()
  id("dagger.hilt.android.plugin")
  id("com.google.gms.google-services")
}

repositories {
  mavenCentral()
  google()
}

android {
  compileSdk = 33
  buildToolsVersion = "33.0.0"

  defaultConfig {
    applicationId = "com.wardellbagby.deltas.android"
    minSdk = 21
    targetSdk = 33
    versionName = AppVersion.versionName
    versionCode = 4

    fun buildConstant(name: String, value: String) {
      manifestPlaceholders[name] = value
      buildConfigField("String", name, "\"$value\"")
    }

    buildConstant("DEEP_LINK_SCHEME", "https")
    buildConstant("DEEP_LINK_HOST", "deltas.app")
    buildConstant("DEEP_LINK_FIREBASE_HOST", "deltas.page.link")
  }

  buildFeatures {
    compose = true
    viewBinding = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Dependencies.kotlinComposeCompilerVersion
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    release {
      postprocessing {
        isRemoveUnusedCode = false
        isRemoveUnusedResources = true
        isObfuscate = false
        isOptimizeCode = true
      }
    }
  }
}

dependencies {
  kapt(Dependencies.HiltCompiler)
  implementation(Dependencies.HiltAndroid)

  implementation(project(":common-models"))

  implementation(Dependencies.KotlinStdlib)
  implementation(Dependencies.RecyclerView)
  implementation(Dependencies.Appcompat)
  implementation(Dependencies.ConstraintLayout)
  implementation(Dependencies.WorkflowUiCoreAndroid)
  implementation(Dependencies.WorkflowTracing)
  implementation(Dependencies.WorkflowUiCompose)
  implementation(Dependencies.Retrofit)
  implementation(Dependencies.KotlinSerialization)
  implementation(Dependencies.KotlinSerializationRetrofitAdapter)
  implementation(Dependencies.LifecycleViewModel)
  implementation(Dependencies.ActivityKtx)
  implementation(Dependencies.ActivityCompose)
  implementation(Dependencies.MaterialCompose)
  implementation(Dependencies.MaterialIconsExtended)

  implementation(Dependencies.GooglePlayServiceAuth)
  implementation(Dependencies.GooglePlayServiceKtx)

  implementation(platform(Dependencies.FirebaseBOM))
  implementation(Dependencies.FirebaseAuth)
  implementation(Dependencies.FirebaseAuthKtx)
  implementation(Dependencies.FirebaseMessaging)
  implementation(Dependencies.FirebaseLinks)
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs =
      freeCompilerArgs + "-opt-in=com.squareup.workflow1.ui.WorkflowUiExperimentalApi"
  }
}
