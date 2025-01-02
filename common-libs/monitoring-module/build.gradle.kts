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
                api(libs.kotlinx.serialization.json)
                
                // Core metrics dependencies that services need
                api(libs.micrometer.core)
                api(libs.micrometer.prometheus)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(project(":common-libs:testing-module"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core monitoring dependencies that services need
                api(libs.ktor.server.call.logging)
                api(libs.ktor.server.call.id)
                api(libs.micrometer.ktor)
                api(libs.slf4j.api)
                
                // Internal dependencies
                implementation(libs.logback)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.status.pages)
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
