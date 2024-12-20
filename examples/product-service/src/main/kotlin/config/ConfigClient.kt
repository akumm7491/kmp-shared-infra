class ConfigClient(private val configServerUrl: String) {
    suspend fun loadConfig(serviceName: String, profile: String = "production"): Config {
        return httpClient.get("$configServerUrl/config/$serviceName/$profile")
    }
}

// Usage in any service:
fun Application.module() {
    val config = ConfigClient("http://config-server:8888")
        .loadConfig("product-service")
    
    // Use configuration
    val databaseUrl = config.getString("database.url")
    val apiKey = config.getString("api.key")
} 