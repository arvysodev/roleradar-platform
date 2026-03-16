package com.roleradar.ingestion.service;

import com.roleradar.ingestion.client.remotive.RemotiveClient;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobResponse;
import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import com.roleradar.ingestion.event.IngestionEventPublisher;
import com.roleradar.ingestion.mapper.RemotiveVacancyMapper;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private final RemotiveClient remotiveClient;
    private final RemotiveVacancyMapper remotiveVacancyMapper;
    private final IngestionEventPublisher ingestionEventPublisher;

    public IngestionService(RemotiveClient remotiveClient, RemotiveVacancyMapper remotiveVacancyMapper, IngestionEventPublisher ingestionEventPublisher) {
        this.remotiveClient = remotiveClient;
        this.remotiveVacancyMapper = remotiveVacancyMapper;
        this.ingestionEventPublisher = ingestionEventPublisher;
    }

    public void ingestRemotiveVacancies() {
        RemotiveJobsResponse response = remotiveClient.getRemoteJobs();

        if (response == null || response.jobs() == null || response.jobs().isEmpty()) {
            return;
        }

        for (RemotiveJobResponse job : response.jobs()) {
            ingestionEventPublisher.publishVacancyUpserted(
                    remotiveVacancyMapper.toEvent(job)
            );
        }
    }
}
