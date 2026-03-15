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
        return Specification.allOf(
                hasSource(filter.source()),
                hasStatus(filter.status()),
                hasRemote(filter.remote())
        );
    }

    public static Specification<Vacancy> hasSource(VacancySource source) {
        if (source == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("source"), source);
    }

    public static Specification<Vacancy> hasStatus(VacancyStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Vacancy> hasRemote(Boolean remote) {
        if (remote == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("remote"), remote);
    }
}
