plugins {
  id("java-library")
  kotlin("jvm")
  kotlinSerialization()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  api(Dependencies.KotlinSerialization)
  api(Dependencies.KotlinDateTime)
}
