package com.roleradar.ingestion.service;

import com.roleradar.ingestion.client.adzuna.AdzunaClient;
import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobResponse;
import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobsResponse;
import com.roleradar.ingestion.client.arbeitnow.ArbeitnowClient;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobResponse;
import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobsResponse;
import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.event.IngestionEventPublisher;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import com.roleradar.ingestion.mapper.AdzunaVacancyMapper;
import com.roleradar.ingestion.mapper.ArbeitnowVacancyMapper;
import com.roleradar.ingestion.mapper.RemotiveVacancyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IngestionService {

    private final RemotiveClient remotiveClient;
    private final RemotiveVacancyMapper remotiveVacancyMapper;
    private final IngestionEventPublisher ingestionEventPublisher;
    private final ArbeitnowClient arbeitnowClient;
    private final ArbeitnowVacancyMapper arbeitnowVacancyMapper;
    private final AdzunaClient adzunaClient;
    private final AdzunaVacancyMapper adzunaVacancyMapper;

    private final int maxJobsPerRunRemotive;
    private final int maxJobsPerRunArbeitnow;
    private final int maxPagesPerRunArbeitnow;

    private final int maxJobsPerRunAdzuna;
    private final int maxPagesPerRunAdzuna;
    private final int resultsPerPageAdzuna;
    private final String adzunaAppId;
    private final String adzunaAppKey;
    private final List<String> adzunaCountries;
    private final List<String> adzunaSearchTerms;

    public IngestionService(RemotiveClient remotiveClient,
                            RemotiveVacancyMapper remotiveVacancyMapper,
                            IngestionEventPublisher ingestionEventPublisher,
                            ArbeitnowClient arbeitnowClient,
                            ArbeitnowVacancyMapper arbeitnowVacancyMapper,
                            AdzunaClient adzunaClient,
                            AdzunaVacancyMapper adzunaVacancyMapper,
                            @Value("${roleradar.ingestion.remotive.max-jobs-per-run}") int maxJobsPerRunRemotive,
                            @Value("${roleradar.ingestion.arbeitnow.max-jobs-per-run}") int maxJobsPerRunArbeitnow,
                            @Value("${roleradar.ingestion.arbeitnow.max-pages-per-run}") int maxPagesPerRunArbeitnow,
                            @Value("${roleradar.ingestion.adzuna.max-jobs-per-run}") int maxJobsPerRunAdzuna,
                            @Value("${roleradar.ingestion.adzuna.max-pages-per-run}") int maxPagesPerRunAdzuna,
                            @Value("${roleradar.ingestion.adzuna.results-per-page}") int resultsPerPageAdzuna,
                            @Value("${roleradar.ingestion.adzuna.app-id}") String adzunaAppId,
                            @Value("${roleradar.ingestion.adzuna.app-key}") String adzunaAppKey,
                            @Value("${roleradar.ingestion.adzuna.countries}") List<String> adzunaCountries,
                            @Value("${roleradar.ingestion.adzuna.search-terms}") List<String> adzunaSearchTerms) {
        this.remotiveClient = remotiveClient;
        this.remotiveVacancyMapper = remotiveVacancyMapper;
        this.ingestionEventPublisher = ingestionEventPublisher;
        this.arbeitnowClient = arbeitnowClient;
        this.arbeitnowVacancyMapper = arbeitnowVacancyMapper;
        this.adzunaClient = adzunaClient;
        this.adzunaVacancyMapper = adzunaVacancyMapper;
        this.maxJobsPerRunRemotive = maxJobsPerRunRemotive;
        this.maxJobsPerRunArbeitnow = maxJobsPerRunArbeitnow;
        this.maxPagesPerRunArbeitnow = maxPagesPerRunArbeitnow;
        this.maxJobsPerRunAdzuna = maxJobsPerRunAdzuna;
        this.maxPagesPerRunAdzuna = maxPagesPerRunAdzuna;
        this.resultsPerPageAdzuna = resultsPerPageAdzuna;
        this.adzunaAppId = adzunaAppId;
        this.adzunaAppKey = adzunaAppKey;
        this.adzunaCountries = adzunaCountries;
        this.adzunaSearchTerms = adzunaSearchTerms;
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

    public IngestionRunResult ingestAdzunaVacancies() {
        int fetched = 0;
        int published = 0;

        for (String country : adzunaCountries) {
            int page = 1;
            int pagesFetched = 0;

            while (pagesFetched < maxPagesPerRunAdzuna && published < maxJobsPerRunAdzuna) {
                AdzunaJobsResponse response = adzunaClient.searchJobs(
                        country,
                        page,
                        adzunaAppId,
                        adzunaAppKey,
                        resultsPerPageAdzuna
                );

                if (response == null || response.results() == null || response.results().isEmpty()) {
                    break;
                }

                fetched += response.results().size();

                for (AdzunaJobResponse job : response.results()) {
                    if (published >= maxJobsPerRunAdzuna) {
                        break;
                    }

                    VacancyUpsertedEvent event = withEventId(
                            adzunaVacancyMapper.toEvent(job)
                    );

                    ingestionEventPublisher.publishVacancyUpserted(event);
                    published++;
                }

                pagesFetched++;

                if (response.results().size() < resultsPerPageAdzuna) {
                    break;
                }

                page++;
            }

            if (published >= maxJobsPerRunAdzuna) {
                break;
            }
        }

        return new IngestionRunResult("ADZUNA", fetched, published);
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
