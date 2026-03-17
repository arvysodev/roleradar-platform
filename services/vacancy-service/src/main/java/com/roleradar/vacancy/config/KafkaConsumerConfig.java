package com.roleradar.vacancy.config;

import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public NewTopic vacancyUpsertedDltTopic(
            @Value("${roleradar.kafka.topics.vacancy-upserted-dlt}") String dltTopic
    ) {
        return new NewTopic(dltTopic, 1, (short) 1);
    }

    @Bean
    public ProducerFactory<String, Object> dltProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(
            ProducerFactory<String, Object> dltProducerFactory
    ) {
        return new KafkaTemplate<>(dltProducerFactory);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, Object> dltKafkaTemplate,
            @Value("${roleradar.kafka.topics.vacancy-upserted-dlt}") String dltTopic
    ) {
        return new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, ex) -> new TopicPartition(dltTopic, record.partition())
        );
    }

    @Bean
    public DefaultErrorHandler vacancyKafkaErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            @Value("${roleradar.kafka.consumer.retry.max-attempts}") long maxAttempts,
            @Value("${roleradar.kafka.consumer.retry.backoff-interval-ms}") long backoffIntervalMs
    ) {
        FixedBackOff backOff = new FixedBackOff(backoffIntervalMs, maxAttempts - 1);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                ConversionException.class,
                SerializationException.class
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VacancyUpsertedEvent> vacancyKafkaListenerContainerFactory(
            ConsumerFactory<String, VacancyUpsertedEvent> consumerFactory,
            DefaultErrorHandler vacancyKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, VacancyUpsertedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(vacancyKafkaErrorHandler);

        return factory;
    }
}
