rootProject.name = "kmp-shared-infra"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// Common libraries
include(":common-libs:auth-module")
include(":common-libs:messaging-module")
include(":common-libs:monitoring-module")
include(":common-libs:networking-module")
include(":common-libs:storage-module")
include(":common-libs:testing-module")
include(":common-libs:validation-module")
include(":common-libs:services-module")

// Core infrastructure services
include(":microservices:api-gateway")
include(":microservices:service-registry")
include(":microservices:config-server")
include(":microservices:monitoring-service")

// Tools
include(":tools")

// Examples
include(":examples:weather-service")
include(":examples:template-service")
