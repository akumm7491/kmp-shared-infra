plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
                implementation(libs.logback)
                implementation(libs.micrometer.core)
                implementation(libs.micrometer.prometheus)
                implementation(libs.micrometer.ktor)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.call.logging)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.ktor.server.call.id)
            }
        }
    }
}
