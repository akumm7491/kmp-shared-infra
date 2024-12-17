rootProject.name = "kmp-shared-infra"

// Common libraries
include(":common-libs:auth-module")
include(":common-libs:messaging-module")
include(":common-libs:storage-module")

// Template service
include(":template-service")

// Microservices
include(":microservices:service-a")
include(":microservices:service-b")

// Tooling
include(":tooling")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
