package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VacancyIngestionService {

    private final VacancyRepository vacancyRepository;

    public VacancyIngestionService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Transactional
    public void upsertVacancy(VacancyUpsertedEvent event) {
        VacancySource source = VacancySource.valueOf(event.source());

        Vacancy vacancy = vacancyRepository.findBySourceAndExternalId(source, event.externalId())
                .map(existing -> {
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
                    return existing;
                })
                .orElseGet(() -> new Vacancy(
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
                ));

        vacancyRepository.save(vacancy);
    }
}
