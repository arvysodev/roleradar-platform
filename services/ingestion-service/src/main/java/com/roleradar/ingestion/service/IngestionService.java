package com.roleradar.ingestion.service;

import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobResponse;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.event.IngestionEventPublisher;
import com.roleradar.ingestion.mapper.RemotiveVacancyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private final RemotiveClient remotiveClient;
    private final RemotiveVacancyMapper remotiveVacancyMapper;
    private final IngestionEventPublisher ingestionEventPublisher;
    private final int maxJobsPerRun;

    public IngestionService(RemotiveClient remotiveClient,
                            RemotiveVacancyMapper remotiveVacancyMapper,
                            IngestionEventPublisher ingestionEventPublisher,
                            @Value("${roleradar.ingestion.remotive.max-jobs-per-run}") int maxJobsPerRun) {
        this.remotiveClient = remotiveClient;
        this.remotiveVacancyMapper = remotiveVacancyMapper;
        this.ingestionEventPublisher = ingestionEventPublisher;
        this.maxJobsPerRun = maxJobsPerRun;
    }

    public IngestionRunResult ingestRemotiveVacancies() {
        RemotiveJobsResponse response = remotiveClient.getRemoteJobs();

        if (response == null || response.jobs() == null || response.jobs().isEmpty()) {
            return new IngestionRunResult("REMOTIVE", 0, 0);
        }

        int fetched = response.jobs().size();

        var events = response.jobs().stream()
                .limit(maxJobsPerRun)
                .map(remotiveVacancyMapper::toEvent)
                .toList();

        events.forEach(ingestionEventPublisher::publishVacancyUpserted);

        return new IngestionRunResult("REMOTIVE", fetched, events.size());
    }
}
