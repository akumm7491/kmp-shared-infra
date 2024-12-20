fun Application.configureServiceDiscovery() {
    install(EurekaClient) {
        serviceName = "order-service"
        serviceUrl = "http://service-registry:8761/eureka"
        
        // Health check endpoint
        healthCheckUrl = "http://localhost:8080/health"
        
        // Service metadata
        metadata = mapOf(
            "version" to "1.0",
            "environment" to "production"
        )
    }
}

// Using service discovery to find other services:
class ProductClient(private val serviceDiscovery: EurekaClient) {
    suspend fun getProduct(id: String): Product {
        val productService = serviceDiscovery.getService("product-service")
        return httpClient.get("${productService.url}/products/$id")
    }
} 