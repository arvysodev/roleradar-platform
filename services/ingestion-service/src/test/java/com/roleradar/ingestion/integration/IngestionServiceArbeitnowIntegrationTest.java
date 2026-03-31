package com.roleradar.ingestion.integration;

import com.roleradar.ingestion.client.adzuna.AdzunaClient;
import com.roleradar.ingestion.client.arbeitnow.ArbeitnowClient;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobResponse;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobsResponse;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionServiceArbeitnowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @MockitoBean
    private ArbeitnowClient arbeitnowClient;

    @MockitoBean
    private IngestionEventPublisher ingestionEventPublisher;

    @MockitoBean
    private RemotiveClient remotiveClient;

    @MockitoBean
    private AdzunaClient adzunaClient;

    @BeforeEach
    void setUp() {
        reset(arbeitnowClient, ingestionEventPublisher, remotiveClient, adzunaClient);
    }

    @Test
    void ingestArbeitnowVacancies_shouldFetchMultiplePagesMapPublishAndReturnRunResult() {
        ArbeitnowJobResponse firstJob = new ArbeitnowJobResponse(
                "backend-engineer-1",
                "Acme",
                "Backend Engineer",
                "<p>Backend Engineer</p>",
                true,
                "https://example.com/jobs/backend-engineer-1",
                List.of("Java", "Spring"),
                List.of("Full-time"),
                "Tallinn",
                1710928800L
        );

        ArbeitnowJobResponse secondJob = new ArbeitnowJobResponse(
                "java-developer-2",
                "Beta",
                "Java Developer",
                "<p>Java Developer</p>",
                false,
                "https://example.com/jobs/java-developer-2",
                List.of("Java"),
                List.of("Full-time"),
                "Berlin",
                1710932400L
        );

        ArbeitnowJobResponse thirdJob = new ArbeitnowJobResponse(
                "senior-backend-3",
                "Gamma",
                "Senior Backend Engineer",
                "<p>Senior Backend Engineer</p>",
                true,
                "https://example.com/jobs/senior-backend-3",
                List.of("Backend"),
                List.of("Full-time"),
                "Remote",
                1710936000L
        );

        ArbeitnowJobsResponse firstPage = new ArbeitnowJobsResponse(
                List.of(firstJob, secondJob),
                new ArbeitnowJobsResponse.Links(
                        "first",
                        "last",
                        null,
                        "next-page"
                ),
                new ArbeitnowJobsResponse.Meta(1, 2)
        );

        ArbeitnowJobsResponse secondPage = new ArbeitnowJobsResponse(
                List.of(thirdJob),
                new ArbeitnowJobsResponse.Links(
                        "first",
                        "last",
                        "prev-page",
                        null
                ),
                new ArbeitnowJobsResponse.Meta(2, 2)
        );

        when(arbeitnowClient.getRemoteJobs(0)).thenReturn(firstPage);
        when(arbeitnowClient.getRemoteJobs(1)).thenReturn(secondPage);

        IngestionRunResult result = ingestionService.ingestArbeitnowVacancies();

        assertThat(result.source()).isEqualTo("ARBEITNOW");
        assertThat(result.fetched()).isEqualTo(3);
        assertThat(result.published()).isEqualTo(3);

        verify(arbeitnowClient, times(1)).getRemoteJobs(0);
        verify(arbeitnowClient, times(1)).getRemoteJobs(1);
        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(org.mockito.ArgumentMatchers.any());

        ArgumentCaptor<VacancyUpsertedEvent> eventCaptor =
                ArgumentCaptor.forClass(VacancyUpsertedEvent.class);

        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(eventCaptor.capture());

        List<VacancyUpsertedEvent> publishedEvents = eventCaptor.getAllValues();

        VacancyUpsertedEvent firstEvent = publishedEvents.getFirst();
        assertThat(firstEvent.eventId()).isNotNull();
        assertThat(firstEvent.source()).isEqualTo("ARBEITNOW");
        assertThat(firstEvent.externalId()).isEqualTo("backend-engineer-1");
        assertThat(firstEvent.title()).isEqualTo("Backend Engineer");
        assertThat(firstEvent.companyName()).isEqualTo("Acme");
        assertThat(firstEvent.location()).isEqualTo("Tallinn");
        assertThat(firstEvent.remote()).isTrue();
        assertThat(firstEvent.url()).isEqualTo("https://example.com/jobs/backend-engineer-1");
        assertThat(firstEvent.descriptionHtml()).isEqualTo("<p>Backend Engineer</p>");
        assertThat(firstEvent.descriptionText()).contains("Backend Engineer");
        assertThat(firstEvent.postedAt()).isEqualTo(LocalDateTime.of(2024, 3, 20, 10, 0));
        assertThat(firstEvent.ingestedAt()).isNotNull();

        VacancyUpsertedEvent secondEvent = publishedEvents.get(1);
        assertThat(secondEvent.eventId()).isNotNull();
        assertThat(secondEvent.source()).isEqualTo("ARBEITNOW");
        assertThat(secondEvent.externalId()).isEqualTo("java-developer-2");
        assertThat(secondEvent.title()).isEqualTo("Java Developer");
        assertThat(secondEvent.companyName()).isEqualTo("Beta");
        assertThat(secondEvent.location()).isEqualTo("Berlin");
        assertThat(secondEvent.remote()).isFalse();
        assertThat(secondEvent.url()).isEqualTo("https://example.com/jobs/java-developer-2");
        assertThat(secondEvent.descriptionHtml()).isEqualTo("<p>Java Developer</p>");
        assertThat(secondEvent.descriptionText()).contains("Java Developer");
        assertThat(secondEvent.postedAt()).isEqualTo(LocalDateTime.of(2024, 3, 20, 11, 0));
        assertThat(secondEvent.ingestedAt()).isNotNull();

        VacancyUpsertedEvent thirdEvent = publishedEvents.get(2);
        assertThat(thirdEvent.eventId()).isNotNull();
        assertThat(thirdEvent.source()).isEqualTo("ARBEITNOW");
        assertThat(thirdEvent.externalId()).isEqualTo("senior-backend-3");
        assertThat(thirdEvent.title()).isEqualTo("Senior Backend Engineer");
        assertThat(thirdEvent.companyName()).isEqualTo("Gamma");
        assertThat(thirdEvent.location()).isEqualTo("Remote");
        assertThat(thirdEvent.remote()).isTrue();
        assertThat(thirdEvent.url()).isEqualTo("https://example.com/jobs/senior-backend-3");
        assertThat(thirdEvent.descriptionHtml()).isEqualTo("<p>Senior Backend Engineer</p>");
        assertThat(thirdEvent.descriptionText()).contains("Senior Backend Engineer");
        assertThat(thirdEvent.postedAt()).isEqualTo(LocalDateTime.of(2024, 3, 20, 12, 0));
        assertThat(thirdEvent.ingestedAt()).isNotNull();
    }

    @Test
    void ingestArbeitnowVacancies_whenNextLinkIsMissing_shouldStopAfterFirstPage() {
        ArbeitnowJobResponse firstJob = new ArbeitnowJobResponse(
                "single-page-job-1",
                "Acme",
                "Backend Engineer",
                "<p>Backend Engineer</p>",
                true,
                "https://example.com/jobs/single-page-job-1",
                List.of("Java"),
                List.of("Full-time"),
                "Tallinn",
                1710928800L
        );

        ArbeitnowJobsResponse singlePage = new ArbeitnowJobsResponse(
                List.of(firstJob),
                new ArbeitnowJobsResponse.Links(
                        "first",
                        "last",
                        null,
                        null
                ),
                new ArbeitnowJobsResponse.Meta(1, 1)
        );

        when(arbeitnowClient.getRemoteJobs(0)).thenReturn(singlePage);

        IngestionRunResult result = ingestionService.ingestArbeitnowVacancies();

        assertThat(result.source()).isEqualTo("ARBEITNOW");
        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.published()).isEqualTo(1);

        verify(arbeitnowClient, times(1)).getRemoteJobs(0);
        verify(ingestionEventPublisher, times(1)).publishVacancyUpserted(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ingestArbeitnowVacancies_shouldRespectMaxJobsPerRunLimitAcrossPages() {
        ArbeitnowJobResponse firstJob = new ArbeitnowJobResponse(
                "job-1",
                "Company 1",
                "Job 1",
                "<p>Job 1</p>",
                true,
                "https://example.com/jobs/job-1",
                List.of(),
                List.of(),
                "Remote",
                1710928800L
        );

        ArbeitnowJobResponse secondJob = new ArbeitnowJobResponse(
                "job-2",
                "Company 2",
                "Job 2",
                "<p>Job 2</p>",
                true,
                "https://example.com/jobs/job-2",
                List.of(),
                List.of(),
                "Remote",
                1710932400L
        );

        ArbeitnowJobResponse thirdJob = new ArbeitnowJobResponse(
                "job-3",
                "Company 3",
                "Job 3",
                "<p>Job 3</p>",
                true,
                "https://example.com/jobs/job-3",
                List.of(),
                List.of(),
                "Remote",
                1710936000L
        );

        ArbeitnowJobResponse fourthJob = new ArbeitnowJobResponse(
                "job-4",
                "Company 4",
                "Job 4",
                "<p>Job 4</p>",
                true,
                "https://example.com/jobs/job-4",
                List.of(),
                List.of(),
                "Remote",
                1710939600L
        );

        ArbeitnowJobsResponse firstPage = new ArbeitnowJobsResponse(
                List.of(firstJob, secondJob),
                new ArbeitnowJobsResponse.Links(
                        "first",
                        "last",
                        null,
                        "next-page"
                ),
                new ArbeitnowJobsResponse.Meta(1, 2)
        );

        ArbeitnowJobsResponse secondPage = new ArbeitnowJobsResponse(
                List.of(thirdJob, fourthJob),
                new ArbeitnowJobsResponse.Links(
                        "first",
                        "last",
                        "prev-page",
                        "next-page-2"
                ),
                new ArbeitnowJobsResponse.Meta(2, 2)
        );

        when(arbeitnowClient.getRemoteJobs(0)).thenReturn(firstPage);
        when(arbeitnowClient.getRemoteJobs(1)).thenReturn(secondPage);

        IngestionRunResult result = ingestionService.ingestArbeitnowVacancies();

        assertThat(result.source()).isEqualTo("ARBEITNOW");
        assertThat(result.fetched()).isEqualTo(4);
        assertThat(result.published()).isEqualTo(3);

        verify(arbeitnowClient, times(1)).getRemoteJobs(0);
        verify(arbeitnowClient, times(1)).getRemoteJobs(1);
        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(org.mockito.ArgumentMatchers.any());

        ArgumentCaptor<VacancyUpsertedEvent> eventCaptor =
                ArgumentCaptor.forClass(VacancyUpsertedEvent.class);

        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(eventCaptor.capture());

        List<VacancyUpsertedEvent> publishedEvents = eventCaptor.getAllValues();

        assertThat(publishedEvents).hasSize(3);
        assertThat(publishedEvents.get(0).externalId()).isEqualTo("job-1");
        assertThat(publishedEvents.get(1).externalId()).isEqualTo("job-2");
        assertThat(publishedEvents.get(2).externalId()).isEqualTo("job-3");
    }
}
