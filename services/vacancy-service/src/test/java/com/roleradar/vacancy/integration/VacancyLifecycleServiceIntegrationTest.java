package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;
import com.roleradar.vacancy.service.VacancyLifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyLifecycleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private VacancyLifecycleService vacancyLifecycleService;

    @Test
    void closeStaleVacancies_shouldCloseOnlyActiveVacanciesOlderThanThreshold() {
        Vacancy staleActiveVacancy = createVacancy(
                "stale-active-1",
                LocalDateTime.now().minusHours(72)
        );

        Vacancy freshActiveVacancy = createVacancy(
                "fresh-active-1",
                LocalDateTime.now().minusHours(12)
        );

        Vacancy alreadyClosedVacancy = createVacancy(
                "already-closed-1",
                LocalDateTime.now().minusHours(100)
        );
        alreadyClosedVacancy.markClosed(LocalDateTime.now().minusDays(5));

        vacancyRepository.saveAllAndFlush(List.of(
                staleActiveVacancy,
                freshActiveVacancy,
                alreadyClosedVacancy
        ));

        vacancyLifecycleService.closeStaleVacancies();

        Vacancy staleAfterCleanup = vacancyRepository.findBySourceAndExternalId(
                VacancySource.REMOTIVE,
                "stale-active-1"
        ).orElseThrow();

        Vacancy freshAfterCleanup = vacancyRepository.findBySourceAndExternalId(
                VacancySource.REMOTIVE,
                "fresh-active-1"
        ).orElseThrow();

        Vacancy alreadyClosedAfterCleanup = vacancyRepository.findBySourceAndExternalId(
                VacancySource.REMOTIVE,
                "already-closed-1"
        ).orElseThrow();

        assertThat(staleAfterCleanup.getStatus()).isEqualTo(VacancyStatus.CLOSED);
        assertThat(staleAfterCleanup.getClosedAt()).isNotNull();

        assertThat(freshAfterCleanup.getStatus()).isEqualTo(VacancyStatus.ACTIVE);
        assertThat(freshAfterCleanup.getClosedAt()).isNull();

        assertThat(alreadyClosedAfterCleanup.getStatus()).isEqualTo(VacancyStatus.CLOSED);
        assertThat(alreadyClosedAfterCleanup.getClosedAt()).isNotNull();
    }

    @Test
    void deleteOldClosedVacancies_shouldDeleteOnlyClosedVacanciesOlderThanRetentionCutoff() {
        Vacancy oldClosedVacancy = createVacancy(
                "old-closed-1",
                LocalDateTime.now().minusDays(50)
        );
        oldClosedVacancy.markClosed(LocalDateTime.now().minusDays(40));

        Vacancy recentClosedVacancy = createVacancy(
                "recent-closed-1",
                LocalDateTime.now().minusDays(10)
        );
        recentClosedVacancy.markClosed(LocalDateTime.now().minusDays(5));

        Vacancy activeVacancy = createVacancy(
                "active-1",
                LocalDateTime.now().minusDays(60)
        );

        vacancyRepository.saveAllAndFlush(List.of(
                oldClosedVacancy,
                recentClosedVacancy,
                activeVacancy
        ));

        vacancyLifecycleService.deleteOldClosedVacancies();

        assertThat(vacancyRepository.findBySourceAndExternalId(VacancySource.REMOTIVE, "old-closed-1"))
                .isEmpty();

        Vacancy recentClosedAfterCleanup = vacancyRepository.findBySourceAndExternalId(
                VacancySource.REMOTIVE,
                "recent-closed-1"
        ).orElseThrow();

        Vacancy activeAfterCleanup = vacancyRepository.findBySourceAndExternalId(
                VacancySource.REMOTIVE,
                "active-1"
        ).orElseThrow();

        assertThat(recentClosedAfterCleanup.getStatus()).isEqualTo(VacancyStatus.CLOSED);
        assertThat(activeAfterCleanup.getStatus()).isEqualTo(VacancyStatus.ACTIVE);
        assertThat(vacancyRepository.count()).isEqualTo(2);
    }

    private Vacancy createVacancy(String externalId, LocalDateTime lastSeenAt) {
        return new Vacancy(
                VacancySource.REMOTIVE,
                externalId,
                "Backend Engineer",
                "Acme",
                "Tallinn",
                true,
                "https://example.com/jobs/" + externalId,
                "<p>Backend Engineer</p>",
                "Backend Engineer",
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                lastSeenAt
        );
    }
}
