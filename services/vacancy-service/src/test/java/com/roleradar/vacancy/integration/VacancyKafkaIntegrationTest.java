package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@EmbeddedKafka(
        topics = "${roleradar.kafka.topics.vacancy-upserted}",
        partitions = 1
)
@DirtiesContext
class VacancyKafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${roleradar.kafka.topics.vacancy-upserted}")
    private String topic;

    @Test
    void consumeVacancyUpsertedEvent_shouldProcessEventAndPersistResults() {
        UUID eventId = UUID.randomUUID();
        String externalId = "kafka-test-123";

        VacancyUpsertedEvent event = new VacancyUpsertedEvent(
                eventId,
                "REMOTIVE",
                externalId,
                "Kafka Backend Engineer",
                "Kafka Corp",
                "Remote",
                true,
                "https://example.com/kafka-job",
                "<p>Kafka description</p>",
                "Kafka description",
                LocalDateTime.of(2026, 3, 22, 10, 0),
                LocalDateTime.of(2026, 3, 22, 10, 5)
        );

        kafkaTemplate.send(topic, eventId.toString(), event);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertThat(vacancyRepository.count()).isEqualTo(1);
                    assertThat(processedEventRepository.existsById(eventId)).isTrue();
                });

        Vacancy savedVacancy = vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, externalId)
                .orElseThrow();

        assertThat(savedVacancy.getTitle()).isEqualTo("Kafka Backend Engineer");
        assertThat(savedVacancy.getCompanyName()).isEqualTo("Kafka Corp");
        assertThat(savedVacancy.getExternalId()).isEqualTo(externalId);
        assertThat(savedVacancy.getSource()).isEqualTo(VacancySource.REMOTIVE);
    }
}
