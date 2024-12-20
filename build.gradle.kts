plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo1.maven.org/maven2/")
        }
    }
}

subprojects {
    group = "com.example.kmp"
    version = "1.0.0"
}
