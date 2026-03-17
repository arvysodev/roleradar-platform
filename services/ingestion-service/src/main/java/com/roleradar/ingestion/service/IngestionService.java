package com.roleradar.ingestion.service;

import com.roleradar.ingestion.client.arbeitnow.ArbeitnowClient;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobResponse;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobsResponse;
import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.event.IngestionEventPublisher;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import com.roleradar.ingestion.mapper.ArbeitnowVacancyMapper;
import com.roleradar.ingestion.mapper.RemotiveVacancyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IngestionService {

    private final RemotiveClient remotiveClient;
    private final RemotiveVacancyMapper remotiveVacancyMapper;
    private final IngestionEventPublisher ingestionEventPublisher;
    private final ArbeitnowClient arbeitnowClient;
    private final ArbeitnowVacancyMapper arbeitnowVacancyMapper;
    private final int maxJobsPerRunRemotive;
    private final int maxJobsPerRunArbeitnow;
    private final int maxPagesPerRunArbeitnow;

    public IngestionService(RemotiveClient remotiveClient,
                            RemotiveVacancyMapper remotiveVacancyMapper,
                            IngestionEventPublisher ingestionEventPublisher,
                            ArbeitnowClient arbeitnowClient,
                            ArbeitnowVacancyMapper arbeitnowVacancyMapper,
                            @Value("${roleradar.ingestion.remotive.max-jobs-per-run}") int maxJobsPerRunRemotive,
                            @Value("${roleradar.ingestion.arbeitnow.max-jobs-per-run}") int maxJobsPerRunArbeitnow,
                            @Value("${roleradar.ingestion.arbeitnow.max-pages-per-run}") int maxPagesPerRunArbeitnow) {
        this.remotiveClient = remotiveClient;
        this.remotiveVacancyMapper = remotiveVacancyMapper;
        this.ingestionEventPublisher = ingestionEventPublisher;
        this.arbeitnowClient = arbeitnowClient;
        this.arbeitnowVacancyMapper = arbeitnowVacancyMapper;
        this.maxJobsPerRunRemotive = maxJobsPerRunRemotive;
        this.maxJobsPerRunArbeitnow = maxJobsPerRunArbeitnow;
        this.maxPagesPerRunArbeitnow = maxPagesPerRunArbeitnow;
    }

    public IngestionRunResult ingestRemotiveVacancies() {
        RemotiveJobsResponse response = remotiveClient.getRemoteJobs();

        if (response == null || response.jobs() == null || response.jobs().isEmpty()) {
            return new IngestionRunResult("REMOTIVE", 0, 0);
        }

        int fetched = response.jobs().size();

        var events = response.jobs().stream()
                .limit(maxJobsPerRunRemotive)
                .map(remotiveVacancyMapper::toEvent)
                .map(this::withEventId)
                .toList();

        events.forEach(ingestionEventPublisher::publishVacancyUpserted);

        return new IngestionRunResult("REMOTIVE", fetched, events.size());
    }

    public IngestionRunResult ingestArbeitnowVacancies() {
        int page = 0;
        int pagesFetched = 0;
        int fetched = 0;
        int published = 0;

        while (pagesFetched < maxPagesPerRunArbeitnow && published < maxJobsPerRunArbeitnow) {
            ArbeitnowJobsResponse response = arbeitnowClient.getRemoteJobs(page);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                break;
            }

            fetched += response.data().size();

            for (ArbeitnowJobResponse job : response.data()) {
                if (published >= maxJobsPerRunArbeitnow) {
                    break;
                }

                VacancyUpsertedEvent event = withEventId(
                        arbeitnowVacancyMapper.toEvent(job)
                );

                ingestionEventPublisher.publishVacancyUpserted(event);
                published++;
            }

            pagesFetched++;

            if (response.links() == null
                    || response.links().next() == null
                    || response.links().next().isBlank()) {
                break;
            }

            page++;
        }

        return new IngestionRunResult("ARBEITNOW", fetched, published);
    }

    private VacancyUpsertedEvent withEventId(VacancyUpsertedEvent event) {
        return new VacancyUpsertedEvent(
                UUID.randomUUID(),
                event.source(),
                event.externalId(),
                event.title(),
                event.companyName(),
                event.location(),
                event.remote(),
                event.url(),
                event.descriptionHtml(),
                event.descriptionText(),
                event.postedAt(),
                event.ingestedAt()
        );
    }
}
