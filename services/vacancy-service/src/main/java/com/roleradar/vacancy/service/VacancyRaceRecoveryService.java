package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.repository.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VacancyRaceRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(VacancyRaceRecoveryService.class);

    private final VacancyRepository vacancyRepository;

    public VacancyRaceRecoveryService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverFromUniqueConstraintRace(VacancySource source, VacancyUpsertedEvent event) {
        Vacancy existing = vacancyRepository.findBySourceAndExternalId(source, event.externalId())
                .orElseThrow(() -> new IllegalStateException(
                        "Vacancy insert race detected but existing vacancy could not be reloaded."
                ));

        existing.refreshFromSource(
                event.title(),
                event.companyName(),
                event.location(),
                event.remote(),
                event.url(),
                event.descriptionHtml(),
                event.descriptionText(),
                event.postedAt(),
                event.ingestedAt(),
                event.ingestedAt()
        );

        vacancyRepository.save(existing);

        log.info(
                "Recovered from vacancy insert race by refreshing existing vacancy: source={} externalId={}",
                source,
                event.externalId()
        );
    }
}
