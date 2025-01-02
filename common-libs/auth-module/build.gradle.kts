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
                api(libs.kotlinx.serialization.json)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
                implementation(project(":common-libs:testing-module"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core auth dependencies that services need
                api(libs.ktor.server.auth)
                api(libs.ktor.server.auth.jwt)
                api(libs.ktor.server.sessions)
                
                // Internal dependencies
                implementation(libs.ktor.server.core)
                implementation(libs.logback)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
