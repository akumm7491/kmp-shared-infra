plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines)
                api(libs.ktor.serialization)
                api(libs.ktor.serialization.json)
                api(libs.koin.core)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core HTTP client dependencies that services need
                api(libs.ktor.client.core)
                api(libs.ktor.client.cio)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.logging)
                
                // Core HTTP server dependencies that services need
                api(libs.ktor.server.core)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.content.negotiation)
                api(libs.ktor.server.cors)
                
                // Core service discovery that services need
                api(libs.netflix.eureka.client)
                api(libs.typesafe.config)
                
                // Core DI dependencies that services need
                api(libs.koin.ktor)
                api(libs.koin.logger)
                
                // Internal dependencies
                implementation(libs.ktor.client.okhttp)  // Alternative HTTP client
                implementation(libs.ktor.server.call.logging)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.netflix.eureka.core)
                implementation(libs.logback)
                
                // Project dependencies
                api(project(":common-libs:monitoring-module"))
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
