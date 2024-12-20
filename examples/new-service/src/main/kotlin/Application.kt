fun Application.module() {
    // 1. Load configuration from Config Server
    val config = ConfigClient("http://config-server:8888")
        .loadConfig("new-service")
    
    // 2. Register with Service Registry
    install(EurekaClient) {
        serviceName = "new-service"
        serviceUrl = "http://service-registry:8761/eureka"
    }
    
    // 3. Configure metrics
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }
    
    // 4. Use other services via service discovery
    val orderClient = OrderClient(environment.serviceDiscovery)
    val paymentClient = PaymentClient(environment.serviceDiscovery)
    
    routing {
        // 5. Expose health check for API Gateway
        get("/health") {
            call.respondText("OK")
        }
        
        // 6. Business endpoints
        post("/checkout") {
            val order = orderClient.createOrder(call.receive())
            val payment = paymentClient.processPayment(order.payment)
            call.respond(CheckoutResponse(order, payment))
        }
    }
} 