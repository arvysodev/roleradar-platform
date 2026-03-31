package com.roleradar.ingestion.integration;

import com.roleradar.ingestion.client.adzuna.AdzunaClient;
import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobResponse;
import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobsResponse;
import com.roleradar.ingestion.client.arbeitnow.ArbeitnowClient;
import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.event.IngestionEventPublisher;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import com.roleradar.ingestion.service.IngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionServiceAdzunaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @MockitoBean
    private AdzunaClient adzunaClient;

    @MockitoBean
    private IngestionEventPublisher ingestionEventPublisher;

    @MockitoBean
    private RemotiveClient remotiveClient;

    @MockitoBean
    private ArbeitnowClient arbeitnowClient;

    @BeforeEach
    void setUp() {
        reset(adzunaClient, ingestionEventPublisher, remotiveClient, arbeitnowClient);
    }

    @Test
    void ingestAdzunaVacancies_shouldFetchAcrossCountriesMapPublishAndReturnRunResult() {
        AdzunaJobResponse firstJob = new AdzunaJobResponse(
                "adzuna-1",
                "Backend Engineer",
                "Remote backend engineering role",
                "2026-03-20T10:00:00Z",
                "https://example.com/jobs/adzuna-1",
                new AdzunaJobResponse.Company("Acme"),
                new AdzunaJobResponse.Location("London", List.of("UK", "London")),
                new AdzunaJobResponse.Category("IT Jobs", "it-jobs"),
                50000,
                70000,
                "permanent",
                "full_time"
        );

        AdzunaJobResponse secondJob = new AdzunaJobResponse(
                "adzuna-2",
                "Java Developer",
                "Office-based backend development",
                "2026-03-20T11:30:00Z",
                "https://example.com/jobs/adzuna-2",
                new AdzunaJobResponse.Company("Beta"),
                new AdzunaJobResponse.Location("Warsaw", List.of("Poland", "Warsaw")),
                new AdzunaJobResponse.Category("Software Jobs", "software-jobs"),
                40000,
                60000,
                "permanent",
                "full_time"
        );

        AdzunaJobsResponse gbPageOne = new AdzunaJobsResponse(List.of(firstJob));
        AdzunaJobsResponse plPageOne = new AdzunaJobsResponse(List.of(secondJob));

        when(adzunaClient.searchJobs("gb", 1, "test-app-id", "test-app-key", 10)).thenReturn(gbPageOne);
        when(adzunaClient.searchJobs("pl", 1, "test-app-id", "test-app-key", 10)).thenReturn(plPageOne);

        IngestionRunResult result = ingestionService.ingestAdzunaVacancies();

        assertThat(result.source()).isEqualTo("ADZUNA");
        assertThat(result.fetched()).isEqualTo(2);
        assertThat(result.published()).isEqualTo(2);

        verify(adzunaClient, times(1)).searchJobs("gb", 1, "test-app-id", "test-app-key", 10);
        verify(adzunaClient, times(1)).searchJobs("pl", 1, "test-app-id", "test-app-key", 10);
        verify(ingestionEventPublisher, times(2)).publishVacancyUpserted(any());

        ArgumentCaptor<VacancyUpsertedEvent> eventCaptor =
                ArgumentCaptor.forClass(VacancyUpsertedEvent.class);

        verify(ingestionEventPublisher, times(2)).publishVacancyUpserted(eventCaptor.capture());

        List<VacancyUpsertedEvent> publishedEvents = eventCaptor.getAllValues();

        VacancyUpsertedEvent firstEvent = publishedEvents.getFirst();
        assertThat(firstEvent.eventId()).isNotNull();
        assertThat(firstEvent.source()).isEqualTo("ADZUNA");
        assertThat(firstEvent.externalId()).isEqualTo("adzuna-1");
        assertThat(firstEvent.title()).isEqualTo("Backend Engineer");
        assertThat(firstEvent.companyName()).isEqualTo("Acme");
        assertThat(firstEvent.location()).isEqualTo("London");
        assertThat(firstEvent.remote()).isTrue();
        assertThat(firstEvent.url()).isEqualTo("https://example.com/jobs/adzuna-1");
        assertThat(firstEvent.descriptionHtml()).contains("Remote backend engineering role");
        assertThat(firstEvent.descriptionText()).isEqualTo("Remote backend engineering role");
        assertThat(firstEvent.postedAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(firstEvent.ingestedAt()).isNotNull();

        VacancyUpsertedEvent secondEvent = publishedEvents.get(1);
        assertThat(secondEvent.eventId()).isNotNull();
        assertThat(secondEvent.source()).isEqualTo("ADZUNA");
        assertThat(secondEvent.externalId()).isEqualTo("adzuna-2");
        assertThat(secondEvent.title()).isEqualTo("Java Developer");
        assertThat(secondEvent.companyName()).isEqualTo("Beta");
        assertThat(secondEvent.location()).isEqualTo("Warsaw");
        assertThat(secondEvent.remote()).isFalse();
        assertThat(secondEvent.url()).isEqualTo("https://example.com/jobs/adzuna-2");
        assertThat(secondEvent.descriptionHtml()).contains("Office-based backend development");
        assertThat(secondEvent.descriptionText()).isEqualTo("Office-based backend development");
        assertThat(secondEvent.postedAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 11, 30));
        assertThat(secondEvent.ingestedAt()).isNotNull();
    }

    @Test
    void ingestAdzunaVacancies_whenResultsAreLessThanPageSize_shouldStopPagingForCountry() {
        AdzunaJobResponse onlyJob = new AdzunaJobResponse(
                "adzuna-single",
                "Hybrid Backend Engineer",
                "Hybrid work opportunity",
                "2026-03-21T09:15:00Z",
                "https://example.com/jobs/adzuna-single",
                new AdzunaJobResponse.Company("Gamma"),
                new AdzunaJobResponse.Location("Tallinn", List.of("Estonia", "Tallinn")),
                new AdzunaJobResponse.Category("IT", "it"),
                45000,
                65000,
                "permanent",
                "full_time"
        );

        when(adzunaClient.searchJobs("gb", 1, "test-app-id", "test-app-key", 10))
                .thenReturn(new AdzunaJobsResponse(List.of(onlyJob)));
        when(adzunaClient.searchJobs("pl", 1, "test-app-id", "test-app-key", 10))
                .thenReturn(new AdzunaJobsResponse(List.of()));

        IngestionRunResult result = ingestionService.ingestAdzunaVacancies();

        assertThat(result.source()).isEqualTo("ADZUNA");
        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.published()).isEqualTo(1);

        verify(adzunaClient, times(1)).searchJobs("gb", 1, "test-app-id", "test-app-key", 10);
        verify(adzunaClient, times(1)).searchJobs("pl", 1, "test-app-id", "test-app-key", 10);
        verify(ingestionEventPublisher, times(1)).publishVacancyUpserted(any());
    }

    @Test
    void ingestAdzunaVacancies_shouldRespectGlobalMaxJobsPerRunAcrossCountries() {
        AdzunaJobResponse gbJobOne = new AdzunaJobResponse(
                "gb-1",
                "Job 1",
                "Remote role one",
                "2026-03-21T10:00:00Z",
                "https://example.com/jobs/gb-1",
                new AdzunaJobResponse.Company("Company 1"),
                new AdzunaJobResponse.Location("London", List.of("UK", "London")),
                new AdzunaJobResponse.Category("IT", "it"),
                1,
                2,
                "permanent",
                "full_time"
        );

        AdzunaJobResponse gbJobTwo = new AdzunaJobResponse(
                "gb-2",
                "Job 2",
                "Remote role two",
                "2026-03-21T11:00:00Z",
                "https://example.com/jobs/gb-2",
                new AdzunaJobResponse.Company("Company 2"),
                new AdzunaJobResponse.Location("London", List.of("UK", "London")),
                new AdzunaJobResponse.Category("IT", "it"),
                1,
                2,
                "permanent",
                "full_time"
        );

        AdzunaJobResponse gbJobThree = new AdzunaJobResponse(
                "gb-3",
                "Job 3",
                "Remote role three",
                "2026-03-21T12:00:00Z",
                "https://example.com/jobs/gb-3",
                new AdzunaJobResponse.Company("Company 3"),
                new AdzunaJobResponse.Location("London", List.of("UK", "London")),
                new AdzunaJobResponse.Category("IT", "it"),
                1,
                2,
                "permanent",
                "full_time"
        );

        AdzunaJobResponse gbJobFour = new AdzunaJobResponse(
                "gb-4",
                "Job 4",
                "Remote role four",
                "2026-03-21T13:00:00Z",
                "https://example.com/jobs/gb-4",
                new AdzunaJobResponse.Company("Company 4"),
                new AdzunaJobResponse.Location("London", List.of("UK", "London")),
                new AdzunaJobResponse.Category("IT", "it"),
                1,
                2,
                "permanent",
                "full_time"
        );

        AdzunaJobsResponse gbPageOne = new AdzunaJobsResponse(List.of(gbJobOne, gbJobTwo, gbJobThree, gbJobFour));

        when(adzunaClient.searchJobs("gb", 1, "test-app-id", "test-app-key", 10)).thenReturn(gbPageOne);

        IngestionRunResult result = ingestionService.ingestAdzunaVacancies();

        assertThat(result.source()).isEqualTo("ADZUNA");
        assertThat(result.fetched()).isEqualTo(4);
        assertThat(result.published()).isEqualTo(3);

        verify(adzunaClient, times(1)).searchJobs("gb", 1, "test-app-id", "test-app-key", 10);

        ArgumentCaptor<VacancyUpsertedEvent> eventCaptor =
                ArgumentCaptor.forClass(VacancyUpsertedEvent.class);

        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(eventCaptor.capture());

        List<VacancyUpsertedEvent> publishedEvents = eventCaptor.getAllValues();

        assertThat(publishedEvents).hasSize(3);
        assertThat(publishedEvents.get(0).externalId()).isEqualTo("gb-1");
        assertThat(publishedEvents.get(1).externalId()).isEqualTo("gb-2");
        assertThat(publishedEvents.get(2).externalId()).isEqualTo("gb-3");
    }
}
