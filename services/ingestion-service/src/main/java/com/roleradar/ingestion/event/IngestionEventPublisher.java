package com.roleradar.ingestion.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class IngestionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String vacancyUpsertedTopic;

    public IngestionEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("ingestion.vacancy-upserted") String vacancyUpsertedTopic
            ) {
        this.kafkaTemplate = kafkaTemplate;
        this.vacancyUpsertedTopic = vacancyUpsertedTopic;
    }

    public void publishVacancyUpserted(VacancyUpsertedEvent event) {
        String key = event.source() + ":" + event.externalId();
        kafkaTemplate.send(vacancyUpsertedTopic, key, event);
    }
}
