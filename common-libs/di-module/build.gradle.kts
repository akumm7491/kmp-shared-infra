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
                api(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)
                api(libs.koin.core)
                
                // Internal dependencies
                api(project(":common-libs:auth-module"))
                api(project(":common-libs:messaging-module"))
                api(project(":common-libs:storage-module"))
                api(project(":common-libs:monitoring-module"))
                api(project(":common-libs:validation-module"))
                api(project(":common-libs:networking-module"))
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Ktor dependencies
                api(libs.ktor.server.core)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.content.negotiation)
                api(libs.ktor.serialization)
                
                // Koin Ktor integration
                api(libs.koin.ktor)
                api(libs.koin.logger)
                
                // Logging
                api(libs.slf4j.api)
                implementation(libs.logback)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(project(":common-libs:testing-module"))
            }
        }
    }
} 