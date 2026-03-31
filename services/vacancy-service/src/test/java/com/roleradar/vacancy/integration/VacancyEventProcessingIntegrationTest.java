package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.event.ProcessedEvent;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.service.VacancyEventProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyEventProcessingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private VacancyEventProcessingService vacancyEventProcessingService;

    @Test
    void processVacancyUpsertEvent_whenEventIsNew_shouldCreateVacancyAndMarkEventProcessed() {
        UUID eventId = UUID.randomUUID();

        VacancyUpsertedEvent event = new VacancyUpsertedEvent(
                eventId,
                "REMOTIVE",
                "remote-123",
                "Backend Engineer",
                "Acme",
                "Tallinn",
                true,
                "https://example.com/jobs/remote-123",
                "<p>Backend Engineer</p>",
                "Backend Engineer",
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 5)
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(event);

        assertThat(vacancyRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        Vacancy savedVacancy = vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, "remote-123")
                .orElseThrow();

        assertThat(savedVacancy.getSource()).isEqualTo(VacancySource.REMOTIVE);
        assertThat(savedVacancy.getExternalId()).isEqualTo("remote-123");
        assertThat(savedVacancy.getTitle()).isEqualTo("Backend Engineer");
        assertThat(savedVacancy.getCompanyName()).isEqualTo("Acme");
        assertThat(savedVacancy.getLocation()).isEqualTo("Tallinn");
        assertThat(savedVacancy.isRemote()).isTrue();
        assertThat(savedVacancy.getUrl()).isEqualTo("https://example.com/jobs/remote-123");
        assertThat(savedVacancy.getDescriptionHtml()).isEqualTo("<p>Backend Engineer</p>");
        assertThat(savedVacancy.getDescriptionText()).isEqualTo("Backend Engineer");
        assertThat(savedVacancy.getPostedAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(savedVacancy.getLastSeenAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 5));

        ProcessedEvent processedEvent = processedEventRepository.findById(eventId).orElseThrow();

        assertThat(processedEvent.getEventId()).isEqualTo(eventId);
        assertThat(processedEvent.getEventType()).isEqualTo("VacancyUpsertedEvent");
        assertThat(processedEvent.getProcessedAt()).isNotNull();
    }

    @Test
    void processVacancyUpsertEvent_whenEventIdIsAlreadyProcessed_shouldSkipProcessing() {
        UUID eventId = UUID.randomUUID();

        VacancyUpsertedEvent event = new VacancyUpsertedEvent(
                eventId,
                "REMOTIVE",
                "remote-456",
                "Java Developer",
                "Company One",
                "Remote",
                true,
                "https://example.com/jobs/remote-456",
                "<p>Java Developer</p>",
                "Java Developer",
                LocalDateTime.of(2026, 3, 20, 11, 0),
                LocalDateTime.of(2026, 3, 20, 11, 5)
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(event);
        vacancyEventProcessingService.processVacancyUpsertEvent(event);

        assertThat(vacancyRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        Vacancy savedVacancy = vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, "remote-456")
                .orElseThrow();

        assertThat(savedVacancy.getTitle()).isEqualTo("Java Developer");
        assertThat(savedVacancy.getCompanyName()).isEqualTo("Company One");
    }

    @Test
    void processVacancyUpsertEvent_whenVacancyAlreadyExistsBySourceAndExternalId_shouldUpdateExistingVacancy() {
        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();

        VacancyUpsertedEvent firstEvent = new VacancyUpsertedEvent(
                firstEventId,
                "REMOTIVE",
                "remote-789",
                "Backend Engineer",
                "Old Company",
                "Tallinn",
                true,
                "https://example.com/jobs/remote-789",
                "<p>Old description</p>",
                "Old description",
                LocalDateTime.of(2026, 3, 20, 12, 0),
                LocalDateTime.of(2026, 3, 20, 12, 5)
        );

        VacancyUpsertedEvent secondEvent = new VacancyUpsertedEvent(
                secondEventId,
                "REMOTIVE",
                "remote-789",
                "Senior Backend Engineer",
                "New Company",
                "Remote",
                false,
                "https://example.com/jobs/remote-789-updated",
                "<p>Updated description</p>",
                "Updated description",
                LocalDateTime.of(2026, 3, 21, 9, 0),
                LocalDateTime.of(2026, 3, 21, 9, 5)
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(firstEvent);
        vacancyEventProcessingService.processVacancyUpsertEvent(secondEvent);

        assertThat(vacancyRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(2);

        Vacancy updatedVacancy = vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, "remote-789")
                .orElseThrow();

        assertThat(updatedVacancy.getTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(updatedVacancy.getCompanyName()).isEqualTo("New Company");
        assertThat(updatedVacancy.getLocation()).isEqualTo("Remote");
        assertThat(updatedVacancy.isRemote()).isFalse();
        assertThat(updatedVacancy.getUrl()).isEqualTo("https://example.com/jobs/remote-789-updated");
        assertThat(updatedVacancy.getDescriptionHtml()).isEqualTo("<p>Updated description</p>");
        assertThat(updatedVacancy.getDescriptionText()).isEqualTo("Updated description");
        assertThat(updatedVacancy.getPostedAt()).isEqualTo(LocalDateTime.of(2026, 3, 21, 9, 0));
        assertThat(updatedVacancy.getLastSeenAt()).isEqualTo(LocalDateTime.of(2026, 3, 21, 9, 5));

        assertThat(processedEventRepository.existsById(firstEventId)).isTrue();
        assertThat(processedEventRepository.existsById(secondEventId)).isTrue();
    }

    @Test
    void processVacancyUpsertEvent_whenSourceIsInvalid_shouldThrowAndPersistNothing() {
        UUID eventId = UUID.randomUUID();

        VacancyUpsertedEvent event = new VacancyUpsertedEvent(
                eventId,
                "INVALID_SOURCE",
                "invalid-1",
                "Backend Engineer",
                "Acme",
                "Tallinn",
                true,
                "https://example.com/jobs/invalid-1",
                "<p>Backend Engineer</p>",
                "Backend Engineer",
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 5)
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> vacancyEventProcessingService.processVacancyUpsertEvent(event)
        );

        assertThat(vacancyRepository.count()).isZero();
        assertThat(processedEventRepository.count()).isZero();
    }

    @Test
    void processVacancyUpsertEvent_whenSameEventIdArrivesWithDifferentPayload_shouldStillBeIgnored() {
        UUID eventId = UUID.randomUUID();

        VacancyUpsertedEvent firstEvent = new VacancyUpsertedEvent(
                eventId,
                "REMOTIVE",
                "remote-999",
                "Backend Engineer",
                "Original Company",
                "Tallinn",
                true,
                "https://example.com/jobs/remote-999",
                "<p>Original description</p>",
                "Original description",
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 5)
        );

        VacancyUpsertedEvent secondEventWithSameId = new VacancyUpsertedEvent(
                eventId,
                "REMOTIVE",
                "remote-999",
                "Changed Title",
                "Changed Company",
                "Remote",
                false,
                "https://example.com/jobs/remote-999-updated",
                "<p>Changed description</p>",
                "Changed description",
                LocalDateTime.of(2026, 3, 21, 9, 0),
                LocalDateTime.of(2026, 3, 21, 9, 5)
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(firstEvent);
        vacancyEventProcessingService.processVacancyUpsertEvent(secondEventWithSameId);

        assertThat(vacancyRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        Vacancy savedVacancy = vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, "remote-999")
                .orElseThrow();

        assertThat(savedVacancy.getTitle()).isEqualTo("Backend Engineer");
        assertThat(savedVacancy.getCompanyName()).isEqualTo("Original Company");
        assertThat(savedVacancy.getLocation()).isEqualTo("Tallinn");
        assertThat(savedVacancy.isRemote()).isTrue();
        assertThat(savedVacancy.getUrl()).isEqualTo("https://example.com/jobs/remote-999");
        assertThat(savedVacancy.getDescriptionHtml()).isEqualTo("<p>Original description</p>");
        assertThat(savedVacancy.getDescriptionText()).isEqualTo("Original description");
    }
}
