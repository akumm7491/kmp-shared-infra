plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines)
                api(libs.ktor.serialization.json)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core Ktor dependencies for service setup
                api(libs.ktor.server.core)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.content.negotiation)
                
                // Common modules - our main dependencies
                api(project(":common-libs:auth-module"))      // Authentication and authorization
                api(project(":common-libs:messaging-module")) // Event handling and messaging
                api(project(":common-libs:monitoring-module")) // Metrics and logging
                api(project(":common-libs:networking-module")) // Service discovery and communication
                api(project(":common-libs:storage-module"))   // Data persistence
                api(project(":common-libs:validation-module")) // Input validation
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":common-libs:testing-module"))
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}
