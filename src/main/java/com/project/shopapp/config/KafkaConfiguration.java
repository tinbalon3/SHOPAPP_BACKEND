package com.project.shopapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Sử dụng KafkaTemplate thay vì KafkaOperations
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 2));
    }

    @Bean
    public NewTopic sendOTPRegisterUser() {
        return new NewTopic("user_register_otp_email_topic", 6, (short) 2);
    }

    @Bean
    public NewTopic orderPaymentsSuccess() {
        return new NewTopic("order-payments-success", 6, (short) 2);
    }

    @Bean
    public NewTopic orderPaymentsFail() {
        return new NewTopic("order-payments-fail", 6, (short) 2);
    }

    @Bean
    public NewTopic handleRetry5m() {
        return new NewTopic("retry_5m_topic", 6, (short) 2);
    }

    @Bean
    public NewTopic handleRetry30m() {
        return new NewTopic("retry_30m_topic", 6, (short) 2);
    }

    @Bean
    public NewTopic handleRetry1h() {
        return new NewTopic("retry_1h_topic", 6, (short) 2);
    }

    @Bean
    public NewTopic failedTopic() {
        return new NewTopic("failed_topic", 6, (short) 2);
    }

    // KafkaProducer configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092"); // Thay đổi nếu bạn sử dụng một Kafka broker khác
        configProps.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put("value.serializer", JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
