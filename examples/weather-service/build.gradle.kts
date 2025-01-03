plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    application
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
    implementation("com.typesafe:config:1.4.2")

    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-netty:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.6")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-cio:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.3")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.6")
}

application {
    mainClass.set("com.example.kmp.weather.ApplicationKt")
}

tasks.shadowJar {
    archiveBaseName.set("weather-service")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}
