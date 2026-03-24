package com.roleradar.ingestion.client.arbeitnow;

import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ArbeitnowClient {

    private final RestClient arbeitnowRestClient;

    public ArbeitnowClient(@Qualifier("arbeitnowRestClient") RestClient arbeitnowRestClient) {
        this.arbeitnowRestClient = arbeitnowRestClient;
    }

    @Retryable(
            includes = {ResourceAccessException.class, RestClientResponseException.class},
            excludes = {ArbeitnowClientException.class},
            maxRetriesString = "${roleradar.ingestion.http.retry.max-retries}",
            delayString = "${roleradar.ingestion.http.retry.delay-millis}"
    )
    public ArbeitnowJobsResponse getRemoteJobs(int page) {
        try {
            return arbeitnowRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/job-board-api")
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .body(ArbeitnowJobsResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new ArbeitnowClientException(
                        "Arbeitnow request failed with client error: " + e.getStatusCode(),
                        e
                );
            }

            throw e;
        }
    }
}
