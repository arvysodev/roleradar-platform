package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.repository.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VacancyIngestionService {

    private static final Logger log = LoggerFactory.getLogger(VacancyIngestionService.class);

    private final VacancyRepository vacancyRepository;
    private final VacancyRaceRecoveryService vacancyRaceRecoveryService;

    public VacancyIngestionService(VacancyRepository vacancyRepository,
                                   VacancyRaceRecoveryService vacancyRaceRecoveryService) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyRaceRecoveryService = vacancyRaceRecoveryService;
    }

    @Transactional
    public void upsertVacancy(VacancyUpsertedEvent event) {
        VacancySource source = VacancySource.valueOf(event.source());

        try {
            vacancyRepository.findBySourceAndExternalId(source, event.externalId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.refreshFromSource(
                                        event.title(),
                                        event.companyName(),
                                        event.location(),
                                        event.remote(),
                                        event.url(),
                                        event.description(),
                                        event.postedAt(),
                                        event.ingestedAt()
                                );

                                vacancyRepository.save(existing);

                                log.info(
                                        "Refreshed existing vacancy: source={} externalId={}",
                                        source,
                                        event.externalId()
                                );
                            },
                            () -> {
                                Vacancy vacancy = new Vacancy(
                                        source,
                                        event.externalId(),
                                        event.title(),
                                        event.companyName(),
                                        event.location(),
                                        event.remote(),
                                        event.url(),
                                        event.description(),
                                        event.postedAt(),
                                        event.ingestedAt()
                                );

                                vacancyRepository.save(vacancy);

                                log.info(
                                        "Created new vacancy: source={} externalId={}",
                                        source,
                                        event.externalId()
                                );
                            }
                    );
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "Detected vacancy upsert race for source={} externalId={}. Recovering in a new transaction.",
                    source,
                    event.externalId()
            );

            vacancyRaceRecoveryService.recoverFromUniqueConstraintRace(source, event);
        }
    }
}
