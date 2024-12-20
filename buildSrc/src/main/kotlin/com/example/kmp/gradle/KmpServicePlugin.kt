package com.example.kmp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("org.jetbrains.kotlin.jvm")
            plugins.apply("io.ktor.plugin")
            plugins.apply("application")

            dependencies {
                // Ktor
                "implementation"(Dependencies.Ktor.serverCore)
                "implementation"(Dependencies.Ktor.serverNetty)
                "implementation"(Dependencies.Ktor.serverAuth)
                "implementation"(Dependencies.Ktor.serverContentNegotiation)
                "implementation"(Dependencies.Ktor.serializationJson)
                
                // Logging
                "implementation"(Dependencies.Logging.logback)
                
                // Metrics
                "implementation"(Dependencies.Ktor.serverMetrics)
                "implementation"(Dependencies.Metrics.micrometer)
                
                // Testing
                "testImplementation"(Dependencies.Ktor.serverTestHost)
                "testImplementation"(Dependencies.Testing.testcontainers)
                "testImplementation"(Dependencies.Testing.testcontainersKafka)
                "testImplementation"(Dependencies.Testing.kotestRunner)
                "testImplementation"(Dependencies.Testing.kotestAssertions)
                "testImplementation"(Dependencies.Testing.mockk)
            }

            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    }
}
