plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClass.set("com.example.kmp.template.ApplicationKt")
}

dependencies {
    // KMP Common Modules
    implementation(project(":common-libs:services-module"))
    implementation(project(":common-libs:messaging-module"))
    implementation(project(":common-libs:monitoring-module"))
    implementation(project(":common-libs:storage-module"))
    implementation(project(":common-libs:auth-module"))
    implementation(project(":common-libs:networking-module"))
    implementation(project(":common-libs:validation-module"))
    implementation(project(":common-libs:di-module"))
    
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.logback)    // Required for logging implementation
    
    // Jakarta Validation
    implementation(libs.jakarta.validation.api)
    implementation(libs.hibernate.validator)
    
    // Testing
    testImplementation(project(":common-libs:testing-module"))
}

tasks.test {
    useJUnitPlatform()
}

// Use shadowJar instead of custom fat JAR task
tasks.shadowJar {
    archiveBaseName.set("template-service")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "com.example.kmp.template.ApplicationKt",
                "Implementation-Title" to "Template Service",
                "Implementation-Version" to project.version
            )
        )
    }
    mergeServiceFiles()
}
