package com.example.kmp.tools

import java.nio.file.Paths

class ServiceGenerator(
    private val serviceName: String,
    private val namespace: String,
    private val port: Int,
    private val modules: List<String>
) {
    private val packageName = serviceName.replace("-", "")
    private val baseDir = Paths.get("examples", serviceName)
    private val sourceDir = baseDir.resolve("src/main/kotlin")
    private val kotlinDir = sourceDir.resolve("com/example/kmp/$packageName")

    fun generate() {
        generateDirectoryStructure()
        generateApplicationKt()
        generateBuildGradle()
        generateDockerfile()
        generateKubernetesFiles()
    }

    private fun generateDirectoryStructure() {
        listOf(
            kotlinDir,
            baseDir.resolve("src/main/resources"),
            baseDir.resolve("src/test/kotlin/com/example/kmp/$packageName"),
            baseDir.resolve("k8s/base")
        ).forEach { it.toFile().mkdirs() }
    }

    private fun generateApplicationKt() {
        val content = """
            package com.example.kmp.$packageName

            import io.ktor.http.HttpStatusCode
            import io.ktor.server.application.Application
            import io.ktor.server.engine.embeddedServer
            import io.ktor.server.netty.Netty
            import io.ktor.server.response.respond
            import io.ktor.server.routing.routing
            import io.ktor.server.routing.get
            import io.ktor.server.metrics.micrometer.MicrometerMetrics
            import io.micrometer.prometheus.PrometheusConfig
            import io.micrometer.prometheus.PrometheusMeterRegistry
            import io.ktor.serialization.kotlinx.json.json
            import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
            import kotlinx.serialization.json.Json
            import kotlinx.coroutines.launch
            ${if (modules.contains("auth")) "import com.example.kmp.auth.TokenValidator" else ""}
            ${if (modules.contains("monitoring")) "import com.example.kmp.monitoring.MonitoringFactory" else ""}
            ${if (modules.contains("messaging")) """
                import com.example.kmp.messaging.EventBus
                import com.example.kmp.messaging.Event
            """.trimIndent() else ""}

            fun main() {
                embeddedServer(Netty, port = $port, host = "0.0.0.0") {
                    module()
                }.start(wait = true)
            }

            fun Application.module() {
                val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
                ${if (modules.contains("auth")) "val tokenValidator = TokenValidator()" else ""}
                ${if (modules.contains("monitoring")) """val logProvider = MonitoringFactory.createLogProvider("$serviceName")""" else ""}
                ${if (modules.contains("messaging")) "val eventBus = EventBus()" else ""}

                install(MicrometerMetrics) {
                    registry = appMicrometerRegistry
                    meterBinders = listOf()
                }

                install(ContentNegotiation) {
                    json(Json { 
                        prettyPrint = true
                        isLenient = true
                    })
                }

                routing {
                    get("/metrics") {
                        call.respond(appMicrometerRegistry.scrape())
                    }

                    get("/health") {
                        call.respond(HttpStatusCode.OK, mapOf("status" to "healthy"))
                    }

                    get("/api/$serviceName") {
                        ${if (modules.contains("auth")) """
                            // Validate token
                            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: "invalid"
                            val validationResult = tokenValidator.validateToken(token)
                            
                            if (!validationResult.isValid) {
                                logProvider.warn("Invalid token attempt", mapOf(
                                    "path" to "/api/$serviceName",
                                    "error" to "Invalid token"
                                ))
                                call.respond(HttpStatusCode.Unauthorized, mapOf(
                                    "error" to "Invalid token",
                                    "timestamp" to System.currentTimeMillis()
                                ))
                                return@get
                            }
                        """.trimIndent() else ""}

                        ${if (modules.contains("monitoring")) """
                            logProvider.info("Handling request", mapOf(
                                "path" to "/api/$serviceName",
                                "method" to "GET",
                                "timestamp" to System.currentTimeMillis()
                            ))
                        """.trimIndent() else ""}

                        ${if (modules.contains("messaging")) """
                            // Publish event asynchronously
                            launch {
                                eventBus.publish("$serviceName-events", Event(
                                    type = "request-processed",
                                    payload = mapOf(
                                        "path" to "/api/$serviceName",
                                        "method" to "GET",
                                        "timestamp" to System.currentTimeMillis()
                                    ),
                                    timestamp = System.currentTimeMillis()
                                ))
                            }
                        """.trimIndent() else ""}

                        call.respond(HttpStatusCode.OK, mapOf(
                            ${if (modules.contains("auth")) """"userId" to validationResult.userId,""" else ""}
                            "message" to "$serviceName is working",
                            "timestamp" to System.currentTimeMillis()
                        ))
                    }
                }
            }
        """.trimIndent()

        kotlinDir.resolve("Application.kt").toFile().writeText(content)
    }

    private fun generateBuildGradle() {
        val modulesList = modules.joinToString("\n") { module ->
            """    implementation(rootProject.project(":common-libs:$module-module"))"""
        }

        val content = """
            plugins {
                kotlin("jvm")
                kotlin("plugin.serialization")
                id("io.ktor.plugin")
                id("application")
            }

            application {
                mainClass.set("com.example.kmp.$packageName.ApplicationKt")
            }

            ktor {
                fatJar {
                    archiveFileName.set("$serviceName-all.jar")
                }
            }

            dependencies {
                // Common modules
            $modulesList

                // Ktor dependencies
                implementation("io.ktor:ktor-server-core:2.3.6")
                implementation("io.ktor:ktor-server-netty:2.3.6")
                implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
                implementation("io.ktor:ktor-server-metrics-micrometer:2.3.6")
                implementation("io.micrometer:micrometer-registry-prometheus:1.11.5")
                implementation("ch.qos.logback:logback-classic:1.4.11")
                
                // Testing
                testImplementation("io.ktor:ktor-server-test-host:2.3.6")
                testImplementation("org.jetbrains.kotlin:kotlin-test")
            }
        """.trimIndent()

        baseDir.resolve("build.gradle.kts").toFile().writeText(content)
    }

    private fun generateDockerfile() {
        val content = """
            FROM gradle:8.5-jdk17 as builder

            WORKDIR /home/gradle/src
            COPY . .

            RUN gradle :examples:$serviceName:buildFatJar --no-daemon

            FROM openjdk:17-slim

            EXPOSE $port

            COPY --from=builder /home/gradle/src/examples/$serviceName/build/libs/$serviceName-all.jar /app/service.jar

            ENTRYPOINT ["java", "-jar", "/app/service.jar"]
        """.trimIndent()

        baseDir.resolve("Dockerfile").toFile().writeText(content)
    }

    private fun generateKubernetesFiles() {
        val deploymentContent = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: $serviceName
              namespace: $namespace
            spec:
              replicas: 1
              selector:
                matchLabels:
                  app: $serviceName
              template:
                metadata:
                  labels:
                    app: $serviceName
                  annotations:
                    prometheus.io/scrape: "true"
                    prometheus.io/port: "$port"
                    prometheus.io/path: "/metrics"
                spec:
                  containers:
                  - name: $serviceName
                    image: $serviceName:latest
                    imagePullPolicy: IfNotPresent
                    ports:
                    - containerPort: $port
                    resources:
                      requests:
                        memory: "256Mi"
                        cpu: "100m"
                      limits:
                        memory: "512Mi"
                        cpu: "200m"
                    readinessProbe:
                      httpGet:
                        path: /health
                        port: $port
                      initialDelaySeconds: 5
                      periodSeconds: 10
                    livenessProbe:
                      httpGet:
                        path: /health
                        port: $port
                      initialDelaySeconds: 15
                      periodSeconds: 20
            ---
            apiVersion: v1
            kind: Service
            metadata:
              name: $serviceName
              namespace: $namespace
            spec:
              selector:
                app: $serviceName
              ports:
              - port: 80
                targetPort: $port
              type: ClusterIP
        """.trimIndent()

        val kustomizationContent = """
            apiVersion: kustomize.config.k8s.io/v1beta1
            kind: Kustomization

            resources:
            - deployment.yaml
        """.trimIndent()

        val k8sDir = baseDir.resolve("k8s/base")
        k8sDir.resolve("deployment.yaml").toFile().writeText(deploymentContent)
        k8sDir.resolve("kustomization.yaml").toFile().writeText(kustomizationContent)
    }
}
