package com.example.kmp.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int

class CreateServiceCommand : CliktCommand(
    help = """
        Create a new KMP service with the specified configuration.
        
        Example:
          create-service --name my-service --type http --modules auth,monitoring --namespace my-project-dev
    """.trimIndent()
) {
    private val name: String by option(
        "-n", "--name",
        help = "Name of the service"
    ).required()
    
    private val namespace: String by option(
        "-ns", "--namespace",
        help = "Kubernetes namespace"
    ).required()
    
    private val type: String by option(
        "-t", "--type",
        help = "Service type (http|event|worker)"
    ).default("http")
    
    private val modules: String by option(
        "-m", "--modules",
        help = "Comma-separated list of modules to include (auth,messaging,monitoring,etc.)"
    ).default("")
    
    private val port: Int by option(
        "-p", "--port",
        help = "Port number"
    ).int().default(8080)

    override fun run() {
        echo("=== Creating service: $name ===")
        echo("Package name: ${name.replace("-", "")}")
        echo("Namespace: $namespace")
        echo("Port: $port")
        echo("Type: $type")
        if (modules.isNotEmpty()) {
            echo("Modules: $modules")
        }

        val modulesList = modules.split(",").filter { it.isNotEmpty() }
        val generator = ServiceGenerator(
            serviceName = name,
            namespace = namespace,
            port = port,
            modules = modulesList
        )

        generator.generate()

        echo("\n=== Service $name created successfully! ===")
        echo("Created files and directories:")
        echo("  - examples/$name/build.gradle.kts")
        echo("  - examples/$name/src/main/kotlin/com/example/kmp/${name.replace("-", "")}/Application.kt")
        echo("  - examples/$name/Dockerfile")
        echo("  - examples/$name/k8s/base/deployment.yaml")
        echo("  - examples/$name/k8s/base/kustomization.yaml")
        echo("\nNext steps:")
        echo("1. Add the service to settings.gradle.kts:")
        echo("   include(\":examples:$name\")")
        echo("\n2. Build the service:")
        echo("   ./gradlew :examples:$name:build")
        echo("\n3. Deploy to Kubernetes:")
        echo("   kubectl apply -k examples/$name/k8s/base")
        echo("\nFor more information, see docs/infrastructure/README.md")
    }
}

fun main(args: Array<String>) = CreateServiceCommand().main(args)
