package com.example.kmp.networking

import io.ktor.server.application.*
import com.example.kmp.networking.models.ServiceRegistryConfig
import com.netflix.eureka.EurekaServerConfig
import com.netflix.eureka.DefaultEurekaServerConfig
import com.netflix.eureka.registry.PeerAwareInstanceRegistry
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl
import com.netflix.eureka.resources.DefaultServerCodecs
import com.netflix.eureka.EurekaServerContext
import com.netflix.eureka.EurekaServerContextHolder
import com.netflix.eureka.DefaultEurekaServerContext
import com.netflix.eureka.cluster.PeerEurekaNodes
import com.netflix.eureka.util.EurekaMonitors
import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.MyDataCenterInstanceConfig
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.discovery.DiscoveryClient
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider

/**
 * Extension function to configure a Ktor application as a Eureka service registry server
 */
fun Application.configureServiceRegistry(config: ServiceRegistryConfig) {
    // Disable client behavior for server
    System.setProperty("eureka.client.registerWithEureka", "false")
    System.setProperty("eureka.client.fetchRegistry", "false")
    
    // Configure server properties
    System.setProperty("eureka.server.enableSelfPreservation", config.enableSelfPreservation.toString())
    System.setProperty("eureka.server.renewalPercentThreshold", config.renewalPercentThreshold.toString())
    System.setProperty("eureka.server.peerEurekaNodesUpdateIntervalMs", "30000")
    System.setProperty("eureka.server.waitTimeInMsWhenSyncEmpty", "0")
    
    // Initialize server components
    val serverConfig: EurekaServerConfig = object : DefaultEurekaServerConfig() {
        override fun getWaitTimeInMsWhenSyncEmpty(): Int = 0
        override fun shouldEnableSelfPreservation(): Boolean = config.enableSelfPreservation
        override fun getRenewalPercentThreshold(): Double = config.renewalPercentThreshold
    }

    // Initialize instance info
    val instanceConfig = MyDataCenterInstanceConfig()
    val instanceInfoProvider = EurekaConfigBasedInstanceInfoProvider(instanceConfig)
    val instanceInfo = instanceInfoProvider.get()
    val applicationInfoManager = ApplicationInfoManager(instanceConfig, instanceInfo)

    // Initialize client config and discovery client
    val clientConfig = DefaultEurekaClientConfig()
    val discoveryClient = DiscoveryClient(applicationInfoManager, clientConfig)

    // Initialize server codecs
    val serverCodecs = DefaultServerCodecs(serverConfig)

    // Initialize registry
    val registry: PeerAwareInstanceRegistry = PeerAwareInstanceRegistryImpl(
        serverConfig,
        clientConfig,
        serverCodecs,
        discoveryClient
    )

    // Initialize peer nodes
    val peerEurekaNodes = PeerEurekaNodes(
        registry,
        serverConfig,
        clientConfig,
        serverCodecs,
        applicationInfoManager
    )

    // Initialize server context
    val serverContext: EurekaServerContext = DefaultEurekaServerContext(
        serverConfig,
        serverCodecs,
        registry,
        peerEurekaNodes,
        applicationInfoManager
    )

    // Initialize monitors
    EurekaMonitors.registerAllStats()

    // Initialize server context
    serverContext.initialize()
    EurekaServerContextHolder.initialize(serverContext)

    // Register shutdown hook
    environment.monitor.subscribe(ApplicationStopping) {
        serverContext.shutdown()
        discoveryClient.shutdown()
    }

    // Log server startup
    log.info("Eureka Server starting with configuration:")
    log.info("- Port: ${config.port}")
    log.info("- Self Preservation: ${config.enableSelfPreservation}")
    log.info("- Renewal Threshold: ${config.renewalPercentThreshold}")
    log.info("- Peer Nodes: ${config.peerEurekaNodes.joinToString()}")
}
