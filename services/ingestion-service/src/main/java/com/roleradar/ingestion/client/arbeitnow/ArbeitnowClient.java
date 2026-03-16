package com.roleradar.ingestion.client.arbeitnow;

import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ArbeitnowClient {

    private final RestClient arbeitnowRestClient;

    public ArbeitnowClient(@Qualifier("arbeitnowRestClient") RestClient arbeitnowRestClient) {
        this.arbeitnowRestClient = arbeitnowRestClient;
    }

    public ArbeitnowJobsResponse getRemoteJobs(int page) {
        return arbeitnowRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/job-board-api")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(ArbeitnowJobsResponse.class);
    }
}
