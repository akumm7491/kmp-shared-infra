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
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.ktor.serialization.json)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core validation dependencies
                api(libs.jakarta.validation.api)
                api(libs.hibernate.validator)
                api(libs.glassfish.el)  // Required for EL implementation
                
                // Ktor integration
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.status.pages)
                
                // Logging
                implementation(libs.logback)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}
