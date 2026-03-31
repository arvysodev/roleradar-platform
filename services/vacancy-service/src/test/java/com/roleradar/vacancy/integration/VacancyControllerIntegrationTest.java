package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.domain.Vacancy;
import com.roleradar.vacancy.domain.VacancySource;
import com.roleradar.vacancy.domain.VacancyStatus;
import com.roleradar.vacancy.event.VacancyUpsertedEvent;
import com.roleradar.vacancy.service.VacancyEventProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VacancyControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VacancyEventProcessingService vacancyEventProcessingService;

    @Test
    void getVacancyById_shouldReturnVacancy() throws Exception {
        Vacancy vacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "remote-1",
                "Backend Engineer",
                "Acme",
                "Tallinn",
                true,
                "https://example.com/jobs/remote-1",
                "<p>Backend role</p>",
                "Backend role",
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 5),
                LocalDateTime.of(2026, 3, 20, 10, 5)
        );

        Vacancy savedVacancy = vacancyRepository.saveAndFlush(vacancy);

        mockMvc.perform(get("/api/v1/vacancies/{id}", savedVacancy.getId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedVacancy.getId().toString()))
                .andExpect(jsonPath("$.source").value("REMOTIVE"))
                .andExpect(jsonPath("$.externalId").value("remote-1"))
                .andExpect(jsonPath("$.title").value("Backend Engineer"))
                .andExpect(jsonPath("$.companyName").value("Acme"))
                .andExpect(jsonPath("$.location").value("Tallinn"))
                .andExpect(jsonPath("$.remote").value(true))
                .andExpect(jsonPath("$.url").value("https://example.com/jobs/remote-1"))
                .andExpect(jsonPath("$.descriptionHtml").value("<p>Backend role</p>"))
                .andExpect(jsonPath("$.descriptionText").value("Backend role"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.closedAt").isEmpty());
    }

    @Test
    void getVacancyById_whenVacancyDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/vacancies/{id}", unknownId)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Vacancy not found."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/not-found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/vacancies/" + unknownId));
    }

    @Test
    void getVacancies_shouldReturnPagedVacanciesSortedByPostedAtDesc() throws Exception {
        Vacancy olderVacancy = new Vacancy(
                VacancySource.ARBEITNOW,
                "arbeitnow-1",
                "Java Developer",
                "Company A",
                "Berlin",
                false,
                "https://example.com/jobs/arbeitnow-1",
                "<p>Java Developer</p>",
                "Java Developer",
                LocalDateTime.of(2026, 3, 18, 9, 0),
                LocalDateTime.of(2026, 3, 18, 9, 5),
                LocalDateTime.of(2026, 3, 18, 9, 5)
        );

        Vacancy newerVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "remotive-2",
                "Senior Backend Engineer",
                "Company B",
                "Remote",
                true,
                "https://example.com/jobs/remotive-2",
                "<p>Senior Backend Engineer</p>",
                "Senior Backend Engineer",
                LocalDateTime.of(2026, 3, 20, 11, 0),
                LocalDateTime.of(2026, 3, 20, 11, 5),
                LocalDateTime.of(2026, 3, 20, 11, 5)
        );

        vacancyRepository.save(olderVacancy);
        vacancyRepository.saveAndFlush(newerVacancy);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].externalId").value("remotive-2"))
                .andExpect(jsonPath("$.items[1].externalId").value("arbeitnow-1"))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(10))
                .andExpect(jsonPath("$.meta.totalItems").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.hasNext").value(false))
                .andExpect(jsonPath("$.meta.hasPrev").value(false));
    }

    @Test
    void getVacancies_shouldFilterBySourceStatusAndRemote() throws Exception {
        Vacancy matchingVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "matching-1",
                "Backend Engineer",
                "Company Match",
                "Remote",
                true,
                "https://example.com/jobs/matching-1",
                "<p>Matching vacancy</p>",
                "Matching vacancy",
                LocalDateTime.of(2026, 3, 20, 12, 0),
                LocalDateTime.of(2026, 3, 20, 12, 5),
                LocalDateTime.of(2026, 3, 20, 12, 5)
        );

        Vacancy wrongSource = new Vacancy(
                VacancySource.ARBEITNOW,
                "other-source",
                "Backend Engineer",
                "Company Other",
                "Berlin",
                true,
                "https://example.com/jobs/other-source",
                "<p>Other source</p>",
                "Other source",
                LocalDateTime.of(2026, 3, 20, 12, 10),
                LocalDateTime.of(2026, 3, 20, 12, 15),
                LocalDateTime.of(2026, 3, 20, 12, 15)
        );

        Vacancy closedVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "closed-1",
                "Closed Backend Engineer",
                "Company Closed",
                "Remote",
                true,
                "https://example.com/jobs/closed-1",
                "<p>Closed vacancy</p>",
                "Closed vacancy",
                LocalDateTime.of(2026, 3, 20, 12, 20),
                LocalDateTime.of(2026, 3, 20, 12, 25),
                LocalDateTime.of(2026, 3, 20, 12, 25)
        );
        closedVacancy.markClosed(LocalDateTime.of(2026, 3, 21, 8, 0));

        Vacancy wrongRemote = new Vacancy(
                VacancySource.REMOTIVE,
                "onsite-1",
                "Onsite Backend Engineer",
                "Company Onsite",
                "Tallinn",
                false,
                "https://example.com/jobs/onsite-1",
                "<p>Onsite vacancy</p>",
                "Onsite vacancy",
                LocalDateTime.of(2026, 3, 20, 12, 30),
                LocalDateTime.of(2026, 3, 20, 12, 35),
                LocalDateTime.of(2026, 3, 20, 12, 35)
        );

        vacancyRepository.save(matchingVacancy);
        vacancyRepository.save(wrongSource);
        vacancyRepository.save(closedVacancy);
        vacancyRepository.saveAndFlush(wrongRemote);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("source", "REMOTIVE")
                        .param("status", VacancyStatus.ACTIVE.name())
                        .param("remote", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("matching-1"))
                .andExpect(jsonPath("$.items[0].source").value("REMOTIVE"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].remote").value(true))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    void getVacancies_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vacancies")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getVacancies_whenStatusIsNotProvided_shouldReturnOnlyActiveVacancies() throws Exception {
        Vacancy activeVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "active-1",
                "Active Backend Engineer",
                "Company Active",
                "Remote",
                true,
                "https://example.com/jobs/active-1",
                "<p>Active vacancy</p>",
                "Active vacancy",
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 5),
                LocalDateTime.of(2026, 3, 20, 10, 5)
        );

        Vacancy closedVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "closed-1",
                "Closed Backend Engineer",
                "Company Closed",
                "Remote",
                true,
                "https://example.com/jobs/closed-1",
                "<p>Closed vacancy</p>",
                "Closed vacancy",
                LocalDateTime.of(2026, 3, 20, 11, 0),
                LocalDateTime.of(2026, 3, 20, 11, 5),
                LocalDateTime.of(2026, 3, 20, 11, 5)
        );
        closedVacancy.markClosed(LocalDateTime.of(2026, 3, 21, 9, 0));

        vacancyRepository.save(activeVacancy);
        vacancyRepository.saveAndFlush(closedVacancy);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("active-1"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    void getVacancies_whenStatusIsClosed_shouldReturnClosedVacancies() throws Exception {
        Vacancy activeVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "active-2",
                "Active Java Engineer",
                "Company Active",
                "Remote",
                true,
                "https://example.com/jobs/active-2",
                "<p>Active Java Engineer</p>",
                "Active Java Engineer",
                LocalDateTime.of(2026, 3, 20, 12, 0),
                LocalDateTime.of(2026, 3, 20, 12, 5),
                LocalDateTime.of(2026, 3, 20, 12, 5)
        );

        Vacancy closedVacancy = new Vacancy(
                VacancySource.ARBEITNOW,
                "closed-2",
                "Closed Java Engineer",
                "Company Closed",
                "Berlin",
                false,
                "https://example.com/jobs/closed-2",
                "<p>Closed Java Engineer</p>",
                "Closed Java Engineer",
                LocalDateTime.of(2026, 3, 20, 13, 0),
                LocalDateTime.of(2026, 3, 20, 13, 5),
                LocalDateTime.of(2026, 3, 20, 13, 5)
        );
        closedVacancy.markClosed(LocalDateTime.of(2026, 3, 21, 10, 0));

        vacancyRepository.save(activeVacancy);
        vacancyRepository.saveAndFlush(closedVacancy);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("status", "CLOSED")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("closed-2"))
                .andExpect(jsonPath("$.items[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    void getVacancies_whenRemoteIsTrueAndStatusIsNotProvided_shouldReturnOnlyActiveRemoteVacancies() throws Exception {
        Vacancy activeRemoteVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "active-remote-1",
                "Remote Backend Engineer",
                "Company Remote",
                "Remote",
                true,
                "https://example.com/jobs/active-remote-1",
                "<p>Remote backend engineer</p>",
                "Remote backend engineer",
                LocalDateTime.of(2026, 3, 20, 14, 0),
                LocalDateTime.of(2026, 3, 20, 14, 5),
                LocalDateTime.of(2026, 3, 20, 14, 5)
        );

        Vacancy closedRemoteVacancy = new Vacancy(
                VacancySource.REMOTIVE,
                "closed-remote-1",
                "Closed Remote Backend Engineer",
                "Company Closed Remote",
                "Remote",
                true,
                "https://example.com/jobs/closed-remote-1",
                "<p>Closed remote vacancy</p>",
                "Closed remote vacancy",
                LocalDateTime.of(2026, 3, 20, 15, 0),
                LocalDateTime.of(2026, 3, 20, 15, 5),
                LocalDateTime.of(2026, 3, 20, 15, 5)
        );
        closedRemoteVacancy.markClosed(LocalDateTime.of(2026, 3, 21, 11, 0));

        Vacancy activeOnsiteVacancy = new Vacancy(
                VacancySource.ARBEITNOW,
                "active-onsite-1",
                "Onsite Backend Engineer",
                "Company Onsite",
                "Tallinn",
                false,
                "https://example.com/jobs/active-onsite-1",
                "<p>Onsite backend engineer</p>",
                "Onsite backend engineer",
                LocalDateTime.of(2026, 3, 20, 16, 0),
                LocalDateTime.of(2026, 3, 20, 16, 5),
                LocalDateTime.of(2026, 3, 20, 16, 5)
        );

        vacancyRepository.save(activeRemoteVacancy);
        vacancyRepository.save(closedRemoteVacancy);
        vacancyRepository.saveAndFlush(activeOnsiteVacancy);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("remote", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("active-remote-1"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].remote").value(true))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    void getVacancies_afterEventProcessing_shouldReturnCreatedActiveVacancyInDefaultListing() throws Exception {
        VacancyUpsertedEvent event = new VacancyUpsertedEvent(
                UUID.randomUUID(),
                "REMOTIVE",
                "event-listed-1",
                "Backend Engineer",
                "Acme",
                "Remote",
                true,
                "https://example.com/jobs/event-listed-1",
                "<p>Backend Engineer</p>",
                "Backend Engineer",
                LocalDateTime.of(2026, 3, 22, 10, 0),
                LocalDateTime.of(2026, 3, 22, 10, 5)
        );

        vacancyEventProcessingService.processVacancyUpsertEvent(event);

        mockMvc.perform(get("/api/v1/vacancies")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("user-1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("event-listed-1"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"));
    }
}
