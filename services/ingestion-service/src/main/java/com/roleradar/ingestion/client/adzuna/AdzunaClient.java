package com.roleradar.ingestion.client.adzuna;

import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AdzunaClient {

    private final RestClient adzunaRestClient;

    public AdzunaClient(@Qualifier("adzunaRestClient") RestClient adzunaRestClient) {
        this.adzunaRestClient = adzunaRestClient;
    }

    @Retryable(
            includes = {ResourceAccessException.class, RestClientResponseException.class},
            excludes = {AdzunaClientException.class},
            maxRetriesString = "${roleradar.ingestion.http.retry.max-retries}",
            delayString = "${roleradar.ingestion.http.retry.delay-millis}"
    )
    public AdzunaJobsResponse searchJobs(String country,
                                         int page,
                                         String appId,
                                         String appKey,
                                         int resultsPerPage) {
        try {
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
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new AdzunaClientException(
                        "Adzuna request failed with client error: " + e.getStatusCode(),
                        e
                );
            }

            throw e;
        }
    }
}
