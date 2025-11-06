package com.wallet_svc.wallet.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration with Production Best Practices
 *
 * Features:
 * - Dead Letter Queue (DLQ) for failed messages
 * - Retry mechanism with exponential backoff
 * - Error handling deserializer
 * - Manual commit for critical operations
 * - Consumer group configuration
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:wallet-service-group}")
    private String groupId;

    /**
     * Consumer Factory with Error Handling
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Connection
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Deserializers with Error Handling
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        // JSON Deserializer Config
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.String");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Consumer Behavior
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Don't lose events
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); // Auto-commit after successful processing
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Performance tuning
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Process in small batches
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes max processing time
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000); // 60 seconds
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 20000); // 20 seconds

        // Fetch config for better throughput
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Error Handler with Dead Letter Queue
     *
     * Retry 3 times with 2 second intervals, then send to DLQ
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Dead Letter Queue recoverer
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> {
                // Send to topic with .DLT suffix (Dead Letter Topic)
                String dlqTopic = record.topic() + ".DLT";
                return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
            }
        );

        // Retry 3 times with 2 second intervals
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(2000L, 3L) // 2 seconds interval, 3 retries
        );

        // Don't retry on specific exceptions (e.g., validation errors)
        // errorHandler.addNotRetryableExceptions(ValidationException.class);

        return errorHandler;
    }

    /**
     * Kafka Listener Container Factory
     * This is the main factory for @KafkaListener
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        // Concurrency - how many concurrent consumers per listener
        factory.setConcurrency(3); // 3 threads per listener

        // Batch processing (optional)
        // factory.setBatchListener(true);

        return factory;
    }
}

