package com.example.kmp.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class IntegrationTest : StringSpec({
    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

    beforeSpec {
        kafka.start()
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.bootstrapServers)
    }

    afterSpec {
        kafka.stop()
    }

    "should be able to send messages to Kafka" {
        val props = Properties().apply {
            put("bootstrap.servers", kafka.bootstrapServers)
            put("key.serializer", StringSerializer::class.java)
            put("value.serializer", StringSerializer::class.java)
        }

        val producer = KafkaProducer<String, String>(props)
        
        val record = ProducerRecord("test-topic", "test-key", "test-value")
        val result = producer.send(record).get()
        
        result.hasOffset() shouldBe true
        producer.close()
    }
})
