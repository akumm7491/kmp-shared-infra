plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.example.kmp.monitoring.ApplicationKt")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization)
    implementation(libs.logback)
    implementation(project(":common-libs:monitoring-module"))
}

ktor {
    fatJar {
        archiveFileName.set("monitoring-service.jar")
    }
} 