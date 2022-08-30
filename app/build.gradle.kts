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
    applicationId = "com.wardellbagby.tracks.android"
    minSdk = 21
    targetSdk = 33
    versionName = AppVersion.versionName
    versionCode = 1
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
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android.txt"))
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
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs =
      freeCompilerArgs + "-opt-in=com.squareup.workflow1.ui.WorkflowUiExperimentalApi"
  }
}
