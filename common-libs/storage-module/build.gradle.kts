plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
                
                // Core database dependencies that services need
                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.jdbc)
                api(libs.hikaricp)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(project(":common-libs:testing-module"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // Core logging that services need
                api(libs.slf4j.api)
                
                // Internal dependencies
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
