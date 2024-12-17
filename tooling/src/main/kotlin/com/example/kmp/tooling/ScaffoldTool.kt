package com.example.kmp.tooling

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ScaffoldTool : CliktCommand() {
    private val serviceName by option(
        help = "Name of the new service"
    ).required()
    
    private val port by option(
        help = "Port number for the new service"
    ).required()

    override fun run() {
        val rootDir = File(System.getProperty("user.dir")).parentFile
        val templateDir = File(rootDir, "template-service")
        val targetDir = File(rootDir, "microservices/$serviceName")

        if (!templateDir.exists()) {
            echo("Template service directory not found!", err = true)
            return
        }

        if (targetDir.exists()) {
            echo("Service directory already exists!", err = true)
            return
        }

        // Copy template service
        templateDir.copyRecursively(targetDir)

        // Update build.gradle.kts
        val buildFile = File(targetDir, "build.gradle.kts")
        if (buildFile.exists()) {
            var content = buildFile.readText()
            content = content.replace(
                "mainClass.set(\"com.example.kmp.template.ApplicationKt\")",
                "mainClass.set(\"com.example.kmp.${serviceName.lowercase()}.ApplicationKt\")"
            )
            buildFile.writeText(content)
        }

        // Update package structure
        val srcDir = File(targetDir, "src/main/kotlin/com/example/kmp")
        val oldPackageDir = File(srcDir, "template")
        val newPackageDir = File(srcDir, serviceName.lowercase())
        oldPackageDir.renameTo(newPackageDir)

        // Update Application.kt
        val applicationFile = File(newPackageDir, "Application.kt")
        if (applicationFile.exists()) {
            var content = applicationFile.readText()
            content = content
                .replace("package com.example.kmp.template", "package com.example.kmp.${serviceName.lowercase()}")
                .replace("port = 8080", "port = $port")
            applicationFile.writeText(content)
        }

        echo("Successfully created new service: $serviceName")
        echo("Location: ${targetDir.absolutePath}")
        echo("Port: $port")
    }
}

fun main(args: Array<String>) = ScaffoldTool().main(args)
