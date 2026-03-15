package com.roleradar.vacancy.repository;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    Optional<Vacancy> findBySourceAndExternalId(VacancySource source, String externalId);

    boolean existsBySourceAndExternalId(VacancySource source, String externalId);
}
