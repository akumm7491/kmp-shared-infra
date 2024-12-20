plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("application")
}

application {
    mainClass.set("com.example.kmp.template.ApplicationKt")
}

dependencies {
    implementation(project(":common-libs:auth-module"))
    implementation(project(":common-libs:messaging-module"))
    implementation(project(":common-libs:storage-module"))

    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-netty:2.3.6")
    implementation("io.ktor:ktor-server-auth:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // Metrics
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.6")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.5")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:2.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.apache.kafka:kafka-clients:3.6.1")
}

tasks.test {
    useJUnitPlatform()
}
