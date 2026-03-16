package com.roleradar.ingestion.client.remotive;

import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RemotiveClient {

    private final RestClient remotiveRestClient;

    public RemotiveClient(RestClient remotiveRestClient) {
        this.remotiveRestClient = remotiveRestClient;
    }

    public RemotiveJobsResponse getRemoteJobs() {
        return remotiveRestClient.get()
                .uri("/api/remote-jobs")
                .retrieve()
                .body(RemotiveJobsResponse.class);
    }
}
