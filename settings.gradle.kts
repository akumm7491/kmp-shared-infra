pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        kotlin("jvm") version "1.9.20"
        kotlin("multiplatform") version "1.9.20"
        kotlin("plugin.serialization") version "1.9.20"
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

rootProject.name = "kmp-shared-infra"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

include(
    ":tools",
    ":common-libs:auth-module",
    ":common-libs:di-module",
    ":common-libs:messaging-module",
    ":common-libs:monitoring-module",
    ":common-libs:networking-module",
    ":common-libs:services-module",
    ":common-libs:storage-module",
    ":common-libs:testing-module",
    ":common-libs:validation-module",
    ":common-libs:cicd-module",
    ":examples:template-service",
    ":examples:weather-service",
    ":microservices:api-gateway",
    ":microservices:config-server",
    ":microservices:monitoring-service",
    ":microservices:service-registry"
)
