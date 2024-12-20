application {
    mainClass.set("com.kmp.shared.infra.gateway.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("api-gateway.jar")
    }
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
        localImageName.set("api-gateway")
        imageTag.set("1.0.0")
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                80,
                8082,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
} 