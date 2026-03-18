package com.roleradar.ingestion.client.adzuna;

import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdzunaClient {

    private final RestClient adzunaRestClient;

    public AdzunaClient(@Qualifier("adzunaRestClient") RestClient adzunaRestClient) {
        this.adzunaRestClient = adzunaRestClient;
    }

    public AdzunaJobsResponse searchJobs(String country,
                                         int page,
                                         String appId,
                                         String appKey,
                                         int resultsPerPage) {
        return adzunaRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/jobs/{country}/search/{page}")
                        .queryParam("app_id", appId)
                        .queryParam("app_key", appKey)
                        .queryParam("results_per_page", resultsPerPage)
                        .queryParam("content-type", "application/json")
                        .build(country, page))
                .retrieve()
                .body(AdzunaJobsResponse.class);
    }
}
