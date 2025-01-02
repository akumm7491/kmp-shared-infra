plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.slf4j.api)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks {
    test {
        useJUnitPlatform()
    }
    
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
} 