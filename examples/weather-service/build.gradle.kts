plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // KMP Common Libraries
    implementation(project(":common-libs:messaging-module"))
    implementation(project(":common-libs:monitoring-module"))
    implementation(project(":common-libs:networking-module"))

    // Service Discovery & Configuration
    implementation(libs.typesafe.config)

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.micrometer.ktor)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // Monitoring
    implementation(libs.micrometer.prometheus)
    implementation(libs.logback)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
}

application {
    mainClass.set("com.example.kmp.weather.ApplicationKt")
}
