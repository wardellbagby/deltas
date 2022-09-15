plugins {
  application
  ktor()
  kotlinJvm()
  kotlinSerialization()
}

application {
  mainClass.set("com.wardellbagby.deltas.server.ServerKt")
}

ktor {
  fatJar {
    archiveFileName.set("tracks-server-${AppVersion.versionName}.jar")
  }
}

tasks.run.configure {
  jvmArgs = listOf("-Dio.ktor.development=true")
}

sourceSets {
  main {
    java {
      srcDirs("src/main/kotlin")
    }
  }

  test {
    java {
      srcDirs("src/test/kotlin")
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":common-models"))

  implementation(Dependencies.KtorServerCore)
  implementation(Dependencies.KtorServerCIO)
  implementation(Dependencies.KtorContentNegotion)
  implementation(Dependencies.KtorSerializationJson)
  implementation(Dependencies.KtorLogging)
  implementation(Dependencies.LogbackClassic)

  implementation(Dependencies.FirebaseAdmin)
  implementation(Dependencies.GooglePlayServiceKtx)
  implementation(Dependencies.Koin)
  implementation(Dependencies.KoinKtor)
}
