plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.example.kmp.api.gateway.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    
    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    
    // Netflix Eureka
    implementation(libs.netflix.eureka.client)
    implementation(libs.netflix.eureka.core)
    
    // Monitoring
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.prometheus)
    implementation(libs.micrometer.ktor)
    implementation(libs.logback)
    
    // Common modules
    implementation(project(":common-libs:networking-module"))
    implementation(project(":common-libs:monitoring-module"))
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:${libs.versions.kotlin.get()}")
    testImplementation(libs.ktor.server.test.host)
}

ktor {
    fatJar {
        archiveFileName.set("api-gateway.jar")
    }
}
