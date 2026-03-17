package com.roleradar.vacancy.repository;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacancyRepository extends JpaRepository<Vacancy, UUID>, JpaSpecificationExecutor<Vacancy> {

    Optional<Vacancy> findBySourceAndExternalId(VacancySource source, String externalId);

    List<Vacancy> findAllByStatusAndLastSeenAtBefore(VacancyStatus status, LocalDateTime cutoff);

    long deleteByStatusAndClosedAtBefore(VacancyStatus status, LocalDateTime cutoff);
}
