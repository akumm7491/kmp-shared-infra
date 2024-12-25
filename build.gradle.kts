plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    kotlin("jvm") version "1.9.20" apply false
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
            sourceSets.all {
                languageSettings.apply {
                    optIn("kotlin.RequiresOptIn")
                    optIn("kotlin.ExperimentalMultiplatform")
                }
            }
        }
    }

    // Configuration for Kotlin JVM projects
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(17)
        }
    }
}
