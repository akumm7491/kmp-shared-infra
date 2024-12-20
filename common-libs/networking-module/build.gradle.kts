plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.serialization.json)
                implementation(libs.koin.core)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Ktor Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                
                // Ktor Server
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.call.logging)
                implementation(libs.ktor.server.status.pages)
                
                // Koin
                implementation(libs.koin.ktor)
                implementation(libs.koin.logger)
                
                implementation(libs.logback)
                
                // Project dependencies
                implementation(project(":common-libs:monitoring-module"))
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
