package com.example.kmp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

class CreateServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register<CreateServiceTask>("createService") {
            group = "kmp"
            description = "Creates a new microservice with KMP infrastructure"
        }
    }
}

open class CreateServiceTask : Copy() {
    init {
        from("../kmp-shared-infra/microservices/demo-service") {
            include("**/*")
            filter { line ->
                line.replace("demo-service", project.properties["serviceName"] as String)
            }
        }
        into("microservices/${project.properties["serviceName"]}")
    }
}
