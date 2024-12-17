plugins {
    kotlin("multiplatform") version "1.9.21" apply false
    kotlin("plugin.serialization") version "1.9.21" apply false
    id("io.ktor.plugin") version "2.3.6" apply false
}

allprojects {
    group = "com.example.kmp"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
