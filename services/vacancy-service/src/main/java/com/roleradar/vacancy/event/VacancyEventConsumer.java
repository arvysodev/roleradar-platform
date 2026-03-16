package com.roleradar.vacancy.event;

import com.roleradar.vacancy.service.VacancyIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VacancyEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(VacancyEventConsumer.class);

    private final VacancyIngestionService vacancyIngestionService;

    public VacancyEventConsumer(VacancyIngestionService vacancyIngestionService) {
        this.vacancyIngestionService = vacancyIngestionService;
    }

    @KafkaListener(
            topics = "${roleradar.kafka.topics.vacancy-upserted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleVacancyUpserted(VacancyUpsertedEvent event) {
        log.info("Consumed vacancy upsert event: source={} externalId={}", event.source(), event.externalId());
        vacancyIngestionService.upsertVacancy(event);
    }
}
