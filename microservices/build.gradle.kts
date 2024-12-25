plugins {
    id("io.ktor.plugin") version "2.3.6" apply false
}

allprojects {
    group = "com.example.kmp"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
    
    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib")
        "implementation"("io.ktor:ktor-server-core:2.3.6")
        "implementation"("ch.qos.logback:logback-classic:1.4.11")
        
        "testImplementation"("org.jetbrains.kotlin:kotlin-test")
        "testImplementation"("io.ktor:ktor-server-test-host:2.3.6")
    }
} 