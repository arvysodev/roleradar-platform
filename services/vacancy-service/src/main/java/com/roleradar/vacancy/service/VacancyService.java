package com.roleradar.vacancy.service;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.dto.VacancyFilter;
import com.roleradar.vacancy.dto.VacancyResponse;
import com.roleradar.vacancy.exception.NotFoundException;
import com.roleradar.vacancy.mapper.VacancyMapper;
import com.roleradar.vacancy.repository.VacancyRepository;
import com.roleradar.vacancy.repository.specification.VacancySpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;

    public VacancyService(VacancyRepository vacancyRepository, VacancyMapper vacancyMapper) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
    }

    @Transactional(readOnly = true)
    public VacancyResponse getVacancyById(UUID id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vacancy not found."));

        return vacancyMapper.toResponse(vacancy);
    }

    @Transactional(readOnly = true)
    public Page<Vacancy> getVacancies(VacancyFilter filter, Pageable pageable) {
        return vacancyRepository.findAll(VacancySpecifications.withFilters(filter), pageable);
    }
}
