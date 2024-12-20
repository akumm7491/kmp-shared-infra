plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.example.kmp.service.registry.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.logback)
    
    // Netflix Eureka
    implementation(libs.netflix.eureka.client)
    implementation(libs.netflix.eureka.core)
    
    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines)
    
    // Common modules
    implementation(project(":common-libs:networking-module"))
}

ktor {
    fatJar {
        archiveFileName.set("service-registry.jar")
    }
} 