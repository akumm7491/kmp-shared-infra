package com.example.kmp.service.registry.registry

import com.example.kmp.service.registry.config.EurekaServerConfig
import com.example.kmp.service.registry.config.ProjectRegistry
import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.config.InvalidServiceNameException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DynamicServiceRegistry(
    private val config: EurekaServerConfig,
    private val projectRegistry: ProjectRegistry
) {
    private val services = mutableMapOf<String, MutableList<ServiceInstance>>()
    private val _servicesFlow = MutableStateFlow<Map<String, List<ServiceInstance>>>(emptyMap())
    val servicesFlow = _servicesFlow.asStateFlow()
    fun registerService(instance: ServiceInstance) {
        projectRegistry.validateService(instance)
        services.getOrPut(instance.app) { mutableListOf() }.add(instance)
        updateServicesFlow()
    }

    fun getService(serviceName: String): List<ServiceInstance>? = services[serviceName]

    fun getAllServices(): Map<String, List<ServiceInstance>> = services.toMap()

    suspend fun startMonitoring() {
        while (true) {
            services.values.flatten().forEach { instance ->
                // TODO: Implement health check logic
                // For now, we'll just keep the services as is
            }
            delay(config.renewalIntervalInSecs * 1000L)
        }
    }

    private fun updateServicesFlow() {
        _servicesFlow.value = services.toMap()
    }
}
