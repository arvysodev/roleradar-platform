package com.roleradar.vacancy.event;

import com.roleradar.vacancy.service.VacancyIngestionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VacancyEventConsumer {

    private final VacancyIngestionService vacancyIngestionService;

    public VacancyEventConsumer(VacancyIngestionService vacancyIngestionService) {
        this.vacancyIngestionService = vacancyIngestionService;
    }

    @KafkaListener(
            topics = "${roleradar.kafka.topics.vacancy-upserted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleVacancyUpserted(VacancyUpsertedEvent event) {
        vacancyIngestionService.upsertVacancy(event);
    }
}
