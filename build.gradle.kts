plugins {
    kotlin("jvm") version "1.9.20" apply false
    kotlin("plugin.serialization") version "1.9.20" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "com.adamkumm"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}

subprojects {
    // Configuration for Kotlin Multiplatform projects
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            jvm {
                jvmToolchain(17)
            }
        }
    }
}
