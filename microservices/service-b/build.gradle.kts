plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("application")
}

application {
    mainClass.set("com.example.kmp.serviceb.ApplicationKt")
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
    
    testImplementation("io.ktor:ktor-server-test-host:2.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
