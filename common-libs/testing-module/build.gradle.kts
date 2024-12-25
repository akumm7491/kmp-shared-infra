plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Core testing dependencies
                api(kotlin("test"))
                api(kotlin("test-junit5"))
                
                // JUnit Platform & Engine
                api(libs.junit.jupiter.api)
                api(libs.junit.jupiter.engine)
                api(libs.junit.jupiter.params)
                api(libs.junit.platform.launcher)
                api(libs.junit.platform.console)
                
                // Kotest Framework
                api(libs.kotest.runner.junit5)
                api(libs.kotest.assertions.core)
                api(libs.kotest.property)
                
                // BDD Testing
                api(libs.cucumber.java)
                api(libs.cucumber.junit.platform)
                
                // Mocking
                api(libs.mockk)
                
                // Async Testing
                api(libs.kotlinx.coroutines)
                api(libs.turbine)
                
                // HTML Report Generation
                api(libs.kotlinx.html)
                
                // Functional Programming
                api(libs.arrow.core)
                
                // Serialization
                api(libs.kotlinx.serialization.json)
                
                // CLI Support
                api(libs.commons.cli)
                
                // Logging
                api(libs.slf4j.api)
                api(libs.logback)
                
                // Ktor Dependencies
                api(libs.ktor.server.test.host)
                api(libs.ktor.client.mock)
                api(libs.ktor.server.content.negotiation)
                api(libs.ktor.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktor.client.cio)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.logging)
                api(libs.ktor.server.core)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.call.logging)
                api(libs.ktor.server.status.pages)
                api(libs.ktor.server.auth)
                api(libs.ktor.server.auth.jwt)
                api(libs.ktor.server.call.id)
                
                // Jackson Dependencies
                api(libs.jackson.core)
                api(libs.jackson.databind)
                api(libs.jackson.module.kotlin)
                
                // Internal Module Dependencies
                api(project(":common-libs:auth-module"))
                api(project(":common-libs:monitoring-module"))
                api(project(":common-libs:messaging-module"))
                api(project(":common-libs:storage-module"))
                api(project(":common-libs:networking-module"))
                api(project(":common-libs:services-module"))
                api(project(":common-libs:validation-module"))
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
