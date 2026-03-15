package com.roleradar.vacancy.controller;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;
import com.roleradar.vacancy.dto.PageResponse;
import com.roleradar.vacancy.dto.VacancyFilter;
import com.roleradar.vacancy.dto.VacancyResponse;
import com.roleradar.vacancy.mapper.VacancyMapper;
import com.roleradar.vacancy.service.VacancyService;
import com.roleradar.vacancy.util.PageResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;

    public VacancyController(VacancyService vacancyService, VacancyMapper vacancyMapper) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
    }

    @GetMapping("/{id}")
    public VacancyResponse getVacancyById(@PathVariable UUID id) {
        return vacancyService.getVacancyById(id);
    }

    @GetMapping
    public PageResponse<VacancyResponse> getVacancies(
            @RequestParam(required = false) VacancySource source,
            @RequestParam(required = false) VacancyStatus status,
            @RequestParam(required = false) Boolean remote,
            @SortDefault(sort = "postedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        VacancyFilter filter = new VacancyFilter(source, status, remote);

        Page<Vacancy> vacancies = vacancyService.getVacancies(filter, pageable);
        return PageResponses.of(vacancies, vacancyMapper::toResponse);
    }
}
