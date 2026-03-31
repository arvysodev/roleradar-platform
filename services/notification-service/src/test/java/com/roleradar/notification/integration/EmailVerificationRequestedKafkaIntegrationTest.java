package com.roleradar.notification.integration;

import com.roleradar.notification.event.EmailVerificationRequestedEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EmbeddedKafka(
        topics = "email-verification-requested",
        partitions = 1
)
@DirtiesContext
class EmailVerificationRequestedKafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, EmailVerificationRequestedEvent> kafkaTemplate;

    @Value("${roleradar.kafka.topics.email-verification-requested}")
    private String topic;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        reset(mailSender);
    }

    @Test
    void handle_shouldConsumeKafkaEventAndSendVerificationEmail() {
        EmailVerificationRequestedEvent event = new EmailVerificationRequestedEvent(
                UUID.randomUUID(),
                "artem@example.com",
                "artem",
                "verification-token-456",
                LocalDateTime.of(2026, 3, 26, 20, 15)
        );

        kafkaTemplate.send(topic, event.userId().toString(), event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> verify(mailSender, times(1)).send(any(SimpleMailMessage.class)));

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly("artem@example.com");
        assertThat(message.getSubject()).isEqualTo("Verify your RoleRadar account");
        assertThat(message.getText()).contains("Hello artem,");
        assertThat(message.getText()).contains(
                "http://localhost:8080/api/v1/auth/verify-email?token=verification-token-456"
        );
    }

    @TestConfiguration
    static class KafkaProducerTestConfiguration {

        @Bean
        ProducerFactory<String, EmailVerificationRequestedEvent> producerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(
                    org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
            );
            props.put(
                    org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class
            );
            props.put(
                    org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    JacksonJsonSerializer.class
            );

            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, EmailVerificationRequestedEvent> kafkaTemplate(
                ProducerFactory<String, EmailVerificationRequestedEvent> producerFactory
        ) {
            return new KafkaTemplate<>(producerFactory);
        }
    }
}
