package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.event.ProcessedEvent;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VacancyEventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VacancyEventProcessingService.class);

    private final ProcessedEventRepository processedEventRepository;
    private final VacancyIngestionService vacancyIngestionService;

    public VacancyEventProcessingService(ProcessedEventRepository processedEventRepository, VacancyIngestionService vacancyIngestionService) {
        this.processedEventRepository = processedEventRepository;
        this.vacancyIngestionService = vacancyIngestionService;
    }

    @Transactional
    public void processVacancyUpsertEvent(VacancyUpsertedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Skipping already processed vacancy event: eventId={}", event.eventId());
            return;
        }

        vacancyIngestionService.upsertVacancy(event);

        processedEventRepository.save(
                new ProcessedEvent(
                        event.eventId(),
                        VacancyUpsertedEvent.class.getSimpleName(),
                        LocalDateTime.now()
                )
        );
    }
}
