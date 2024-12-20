fun Application.configureMetrics() {
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        
        // Custom metrics
        counter("payment.attempts.total")
        gauge("payment.processing.current") { /* current processing count */ }
        timer("payment.processing.time")
    }
}

class PaymentService {
    private val paymentTimer = Metrics.timer("payment.processing.time")
    
    suspend fun processPayment(payment: Payment) {
        paymentTimer.record {
            // Process payment
            Metrics.counter("payment.attempts.total").increment()
            // ... payment processing logic
        }
    }
} 