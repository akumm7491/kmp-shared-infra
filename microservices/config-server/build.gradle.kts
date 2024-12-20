plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.example.kmp.config.server.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("config-server.jar")
    }
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
    implementation(libs.kotlinx.coroutines)
    
    // Config parsing
    implementation("com.typesafe:config:1.4.2")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
}
