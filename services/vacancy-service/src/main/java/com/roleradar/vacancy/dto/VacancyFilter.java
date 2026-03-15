package com.roleradar.vacancy.dto;

import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;

public record VacancyFilter (
        VacancySource source,
        VacancyStatus status,
        Boolean remote
){
}
