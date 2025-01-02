plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(project(":common-libs:testing-module"))
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core messaging dependencies that services need
                api(libs.kafka.clients)
                api(libs.kafka.schema.registry)
                api(libs.kafka.avro.serializer)
                api(libs.avro)
                
                // Internal dependencies
                implementation(libs.kotlin.reflect)
                implementation(libs.reflections)
                implementation(libs.slf4j.api)
                implementation(libs.logback)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(project(":common-libs:testing-module"))
            }
        }
    }
}
