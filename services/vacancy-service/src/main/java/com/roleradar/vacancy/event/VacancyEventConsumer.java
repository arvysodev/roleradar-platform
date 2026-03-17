package com.roleradar.vacancy.event;

import com.roleradar.vacancy.service.VacancyEventProcessingService;
import com.roleradar.vacancy.service.VacancyIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VacancyEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(VacancyEventConsumer.class);

    private final VacancyEventProcessingService vacancyEventProcessingService;

    public VacancyEventConsumer(VacancyEventProcessingService vacancyEventProcessingService) {
        this.vacancyEventProcessingService = vacancyEventProcessingService;
    }

    @KafkaListener(
            topics = "${roleradar.kafka.topics.vacancy-upserted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeVacancyUpserted(VacancyUpsertedEvent event) {
        log.info(
                "Consumed vacancy upsert event: eventId={} source={} externalId={}",
                event.eventId(),
                event.source(),
                event.externalId()
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(event);
    }
}
