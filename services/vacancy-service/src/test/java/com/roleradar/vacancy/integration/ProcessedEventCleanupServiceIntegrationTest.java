package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.domain.event.ProcessedEvent;
import com.roleradar.vacancy.service.ProcessedEventCleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedEventCleanupServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProcessedEventCleanupService processedEventCleanupService;

    @Test
    void deleteOldProcessedEvents_shouldDeleteOnlyEventsOlderThanRetentionCutoff() {
        UUID oldEventId = UUID.randomUUID();
        UUID recentEventId = UUID.randomUUID();

        ProcessedEvent oldEvent = new ProcessedEvent(
                oldEventId,
                "VacancyUpsertedEvent",
                LocalDateTime.now().minusDays(40)
        );

        ProcessedEvent recentEvent = new ProcessedEvent(
                recentEventId,
                "VacancyUpsertedEvent",
                LocalDateTime.now().minusDays(5)
        );

        processedEventRepository.saveAndFlush(oldEvent);
        processedEventRepository.saveAndFlush(recentEvent);

        processedEventCleanupService.deleteOldProcessedEvents();

        assertThat(processedEventRepository.existsById(oldEventId)).isFalse();
        assertThat(processedEventRepository.existsById(recentEventId)).isTrue();
        assertThat(processedEventRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteOldProcessedEvents_whenNothingMatches_shouldLeaveEventsUntouched() {
        UUID recentEventId = UUID.randomUUID();

        ProcessedEvent recentEvent = new ProcessedEvent(
                recentEventId,
                "VacancyUpsertedEvent",
                LocalDateTime.now().minusDays(3)
        );

        processedEventRepository.saveAndFlush(recentEvent);

        processedEventCleanupService.deleteOldProcessedEvents();

        assertThat(processedEventRepository.existsById(recentEventId)).isTrue();
        assertThat(processedEventRepository.count()).isEqualTo(1);
    }
}
