package com.roleradar.vacancy.mapper;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.dto.VacancyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "status", expression = "java(vacancy.getStatus().name())")
    VacancyResponse toResponse(Vacancy vacancy);
}
