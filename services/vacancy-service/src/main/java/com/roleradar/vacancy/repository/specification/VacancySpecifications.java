package com.roleradar.vacancy.repository.specification;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;
import com.roleradar.vacancy.dto.VacancyFilter;
import org.springframework.data.jpa.domain.Specification;

public final class VacancySpecifications {

    private VacancySpecifications() {
    }

    public static Specification<Vacancy> withFilters(VacancyFilter filter) {
        Specification<Vacancy> specification = hasStatus(filter.status());

        Specification<Vacancy> sourceSpecification = hasSource(filter.source());
        if (sourceSpecification != null) {
            specification = specification.and(sourceSpecification);
        }

        Specification<Vacancy> remoteSpecification = hasRemote(filter.remote());
        if (remoteSpecification != null) {
            specification = specification.and(remoteSpecification);
        }

        return specification;
    }

    public static Specification<Vacancy> hasSource(VacancySource source) {
        if (source == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("source"), source);
    }

    public static Specification<Vacancy> hasStatus(VacancyStatus status) {
        VacancyStatus effectiveStatus = status != null ? status : VacancyStatus.ACTIVE;
        return (root, query, cb) -> cb.equal(root.get("status"), effectiveStatus);
    }

    public static Specification<Vacancy> hasRemote(Boolean remote) {
        if (remote == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("remote"), remote);
    }
}
