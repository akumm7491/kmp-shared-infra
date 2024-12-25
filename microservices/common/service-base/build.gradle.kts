plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines)
    
    // Ktor server dependencies
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.id)
    
    // Ktor client for health checks
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    
    // Logging and monitoring
    implementation(libs.logback)
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.prometheus)
    implementation(libs.micrometer.ktor)
    
    // Common modules
    implementation(project(":common-libs:monitoring-module"))
    implementation(project(":common-libs:networking-module"))
    implementation(project(":common-libs:messaging-module"))
    
    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.mockk)
}
