buildscript {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }

  dependencies {
    classpath(Dependencies.AndroidGradlePlugin)
    classpath(Dependencies.KotlinGradlePlugin)
    classpath(Dependencies.HiltGradlePlugin)
    classpath(Dependencies.KtlintGradlePlugin)
    classpath(Dependencies.GoogleServices)
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }

  apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
