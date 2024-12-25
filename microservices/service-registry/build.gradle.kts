plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    // Common modules
    implementation(project(":common-libs:auth-module"))
    implementation(project(":common-libs:messaging-module"))
    implementation(project(":common-libs:monitoring-module"))
    implementation(project(":common-libs:networking-module"))
    implementation(project(":common-libs:services-module"))
    implementation(project(":common-libs:storage-module"))
    implementation(project(":common-libs:validation-module"))

    // Core dependencies
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Arrow dependencies
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // Validation dependencies
    implementation(libs.jakarta.validation.api)
    implementation(libs.hibernate.validator)
    implementation("org.glassfish:jakarta.el:4.0.2") // Required for Hibernate Validator

    // Ktor server dependencies
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.typesafe.config)

    // Ktor client dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)

    // Netflix Eureka dependencies
    implementation(libs.netflix.eureka.client)
    implementation(libs.netflix.eureka.core)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.logback)
    implementation(libs.logback.core)
    implementation(libs.logback.contrib.json.classic)
    implementation(libs.logback.contrib.jackson)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)

    // Testing dependencies
    testImplementation(project(":common-libs:testing-module"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.example.kmp.service.registry.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("service-registry.jar")
    }
}

