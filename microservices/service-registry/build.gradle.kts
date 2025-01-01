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
    implementation(project(":common-libs:services-module"))
    implementation(project(":common-libs:monitoring-module"))
    implementation(project(":common-libs:networking-module"))
    
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.logback)
    implementation(libs.slf4j.api)
    
    implementation(libs.jakarta.validation.api)
    implementation(libs.hibernate.validator)
    implementation(libs.glassfish.el)
    
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)
    
    testImplementation(project(":common-libs:testing-module"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.mockk)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn",
            "-Xexpect-actual-classes"
        )
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

