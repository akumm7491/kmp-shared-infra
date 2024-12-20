package com.example.kmp.gradle

object Versions {
    const val kotlin = "1.9.20"
    const val ktor = "2.3.6"
    const val logback = "1.4.11"
    const val micrometer = "1.11.5"
    const val testcontainers = "1.19.3"
    const val kotest = "5.8.0"
    const val mockk = "1.13.8"
    const val kafka = "3.6.1"
}

object Dependencies {
    object Ktor {
        const val serverCore = "io.ktor:ktor-server-core:${Versions.ktor}"
        const val serverNetty = "io.ktor:ktor-server-netty:${Versions.ktor}"
        const val serverAuth = "io.ktor:ktor-server-auth:${Versions.ktor}"
        const val serverContentNegotiation = "io.ktor:ktor-server-content-negotiation:${Versions.ktor}"
        const val serializationJson = "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}"
        const val serverMetrics = "io.ktor:ktor-server-metrics-micrometer:${Versions.ktor}"
        const val serverTestHost = "io.ktor:ktor-server-test-host:${Versions.ktor}"
    }

    object Logging {
        const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
    }

    object Metrics {
        const val micrometer = "io.micrometer:micrometer-registry-prometheus:${Versions.micrometer}"
    }

    object Testing {
        const val testcontainers = "org.testcontainers:testcontainers:${Versions.testcontainers}"
        const val testcontainersKafka = "org.testcontainers:kafka:${Versions.testcontainers}"
        const val kotestRunner = "io.kotest:kotest-runner-junit5:${Versions.kotest}"
        const val kotestAssertions = "io.kotest:kotest-assertions-core:${Versions.kotest}"
        const val mockk = "io.mockk:mockk:${Versions.mockk}"
        const val kafka = "org.apache.kafka:kafka-clients:${Versions.kafka}"
    }
}
