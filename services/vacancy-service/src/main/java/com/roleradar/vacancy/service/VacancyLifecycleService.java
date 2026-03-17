package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancyStatus;
import com.roleradar.vacancy.repository.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VacancyLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(VacancyLifecycleService.class);

    private final VacancyRepository vacancyRepository;
    private final long staleThresholdHours;
    private final long closedThresholdDays;


    public VacancyLifecycleService(
            VacancyRepository vacancyRepository,
            @Value("${roleradar.vacancy.lifecycle.stale-close-threshold-hours}") long staleThresholdHours,
            @Value("${roleradar.vacancy.lifecycle.closed-retention-days}") long closedThresholdDays
    ) {
        this.vacancyRepository = vacancyRepository;
        this.staleThresholdHours = staleThresholdHours;
        this.closedThresholdDays = closedThresholdDays;
    }

    @Transactional
    @Scheduled(cron = "${roleradar.vacancy.lifecycle.stale-close-cron}")
    public void closeStaleVacancies() {
        LocalDateTime cutOff = LocalDateTime.now().minusHours(staleThresholdHours);

        List<Vacancy> staleVacancies = vacancyRepository.findAllByStatusAndLastSeenAtBefore(
                VacancyStatus.ACTIVE,
                cutOff
        );

        if (staleVacancies.isEmpty()) return;

        LocalDateTime closedAt = LocalDateTime.now();

        for (Vacancy vacancy : staleVacancies) {
            vacancy.markClosed(closedAt);
        }

        vacancyRepository.saveAll(staleVacancies);

        log.info(
                "Closed stale vacancies: count={} cutoff={}",
                staleVacancies.size(),
                cutOff
        );
    }

    @Transactional
    @Scheduled(cron = "${roleradar.vacancy.lifecycle.closed-cleanup-cron}")
    public void deleteOldClosedVacancies() {
        LocalDateTime cutOff = LocalDateTime.now().minusDays(closedThresholdDays);

        long deleted = vacancyRepository.deleteByStatusAndClosedAtBefore(
                VacancyStatus.ACTIVE,
                cutOff
        );

        if (deleted > 0) {
            log.info(
                    "Deleted old closed vacancies: count={} cutoff={}",
                    deleted,
                    cutOff
            );
        }
    }
}
