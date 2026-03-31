package com.roleradar.ingestion.integration;

import com.roleradar.ingestion.client.adzuna.AdzunaClient;
import com.roleradar.ingestion.client.arbeitnow.ArbeitnowClient;
import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobResponse;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
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

class IngestionServiceRemotiveIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @MockitoBean
    private RemotiveClient remotiveClient;

    @MockitoBean
    private IngestionEventPublisher ingestionEventPublisher;

    @MockitoBean
    private ArbeitnowClient arbeitnowClient;

    @MockitoBean
    private AdzunaClient adzunaClient;

    @BeforeEach
    void setUp() {
        reset(remotiveClient, ingestionEventPublisher, arbeitnowClient, adzunaClient);
    }

    @Test
    void ingestRemotiveVacancies_shouldMapPublishAndReturnRunResult() {
        RemotiveJobResponse firstJob = new RemotiveJobResponse(
                101L,
                "https://example.com/jobs/101",
                "Backend Engineer",
                "Acme",
                "Remote",
                "<p>Backend Engineer</p>",
                "2026-03-20T10:00:00"
        );

        RemotiveJobResponse secondJob = new RemotiveJobResponse(
                102L,
                "https://example.com/jobs/102",
                "Senior Java Developer",
                "Beta",
                "Europe",
                "<p>Senior Java Developer</p>",
                "2026-03-21T11:30:00"
        );

        when(remotiveClient.getRemoteJobs()).thenReturn(
                new RemotiveJobsResponse(List.of(firstJob, secondJob))
        );

        IngestionRunResult result = ingestionService.ingestRemotiveVacancies();

        assertThat(result.source()).isEqualTo("REMOTIVE");
        assertThat(result.fetched()).isEqualTo(2);
        assertThat(result.published()).isEqualTo(2);

        ArgumentCaptor<VacancyUpsertedEvent> eventCaptor =
                ArgumentCaptor.forClass(VacancyUpsertedEvent.class);

        verify(ingestionEventPublisher, times(2)).publishVacancyUpserted(eventCaptor.capture());

        List<VacancyUpsertedEvent> publishedEvents = eventCaptor.getAllValues();

        VacancyUpsertedEvent firstEvent = publishedEvents.getFirst();
        assertThat(firstEvent.eventId()).isNotNull();
        assertThat(firstEvent.source()).isEqualTo("REMOTIVE");
        assertThat(firstEvent.externalId()).isEqualTo("101");
        assertThat(firstEvent.title()).isEqualTo("Backend Engineer");
        assertThat(firstEvent.companyName()).isEqualTo("Acme");
        assertThat(firstEvent.location()).isEqualTo("Remote");
        assertThat(firstEvent.remote()).isTrue();
        assertThat(firstEvent.url()).isEqualTo("https://example.com/jobs/101");
        assertThat(firstEvent.descriptionHtml()).isEqualTo("<p>Backend Engineer</p>");
        assertThat(firstEvent.descriptionText()).contains("Backend Engineer");
        assertThat(firstEvent.postedAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(firstEvent.ingestedAt()).isNotNull();

        VacancyUpsertedEvent secondEvent = publishedEvents.get(1);
        assertThat(secondEvent.eventId()).isNotNull();
        assertThat(secondEvent.source()).isEqualTo("REMOTIVE");
        assertThat(secondEvent.externalId()).isEqualTo("102");
        assertThat(secondEvent.title()).isEqualTo("Senior Java Developer");
        assertThat(secondEvent.companyName()).isEqualTo("Beta");
        assertThat(secondEvent.location()).isEqualTo("Europe");
        assertThat(secondEvent.remote()).isTrue();
        assertThat(secondEvent.url()).isEqualTo("https://example.com/jobs/102");
        assertThat(secondEvent.descriptionHtml()).isEqualTo("<p>Senior Java Developer</p>");
        assertThat(secondEvent.descriptionText()).contains("Senior Java Developer");
        assertThat(secondEvent.postedAt()).isEqualTo(LocalDateTime.of(2026, 3, 21, 11, 30));
        assertThat(secondEvent.ingestedAt()).isNotNull();
    }

    @Test
    void ingestRemotiveVacancies_whenResponseIsEmpty_shouldReturnZeroResultAndPublishNothing() {
        when(remotiveClient.getRemoteJobs()).thenReturn(new RemotiveJobsResponse(List.of()));

        IngestionRunResult result = ingestionService.ingestRemotiveVacancies();

        assertThat(result.source()).isEqualTo("REMOTIVE");
        assertThat(result.fetched()).isZero();
        assertThat(result.published()).isZero();

        verify(ingestionEventPublisher, times(0)).publishVacancyUpserted(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ingestRemotiveVacancies_shouldRespectMaxJobsPerRunLimit() {
        RemotiveJobResponse firstJob = new RemotiveJobResponse(
                201L,
                "https://example.com/jobs/201",
                "Job 1",
                "Company 1",
                "Remote",
                "<p>Job 1</p>",
                "2026-03-20T10:00:00"
        );

        RemotiveJobResponse secondJob = new RemotiveJobResponse(
                202L,
                "https://example.com/jobs/202",
                "Job 2",
                "Company 2",
                "Remote",
                "<p>Job 2</p>",
                "2026-03-20T11:00:00"
        );

        RemotiveJobResponse thirdJob = new RemotiveJobResponse(
                203L,
                "https://example.com/jobs/203",
                "Job 3",
                "Company 3",
                "Remote",
                "<p>Job 3</p>",
                "2026-03-20T12:00:00"
        );

        RemotiveJobResponse fourthJob = new RemotiveJobResponse(
                204L,
                "https://example.com/jobs/204",
                "Job 4",
                "Company 4",
                "Remote",
                "<p>Job 4</p>",
                "2026-03-20T13:00:00"
        );

        when(remotiveClient.getRemoteJobs()).thenReturn(
                new RemotiveJobsResponse(List.of(firstJob, secondJob, thirdJob, fourthJob))
        );

        IngestionRunResult result = ingestionService.ingestRemotiveVacancies();

        assertThat(result.source()).isEqualTo("REMOTIVE");
        assertThat(result.fetched()).isEqualTo(4);
        assertThat(result.published()).isEqualTo(3);

        verify(ingestionEventPublisher, times(3)).publishVacancyUpserted(org.mockito.ArgumentMatchers.any());
    }
}
