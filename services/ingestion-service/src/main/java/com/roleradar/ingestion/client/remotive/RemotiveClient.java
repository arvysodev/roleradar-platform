package com.roleradar.ingestion.client.remotive;

import com.roleradar.ingestion.client.remotive.dto.RemotiveJobsResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RemotiveClient {

    private final RestClient remotiveRestClient;

    public RemotiveClient(RestClient remotiveRestClient) {
        this.remotiveRestClient = remotiveRestClient;
    }

    @Retryable(
            includes = {ResourceAccessException.class, RestClientResponseException.class},
            excludes = {RemotiveClientException.class},
            maxRetriesString = "${roleradar.ingestion.http.retry.max-retries}",
            delayString = "${roleradar.ingestion.http.retry.delay-millis}"
    )
    public RemotiveJobsResponse getRemoteJobs() {
        try {
            return remotiveRestClient.get()
                    .uri("/api/remote-jobs")
                    .retrieve()
                    .body(RemotiveJobsResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new RemotiveClientException(
                        "Remotive request failed with client error: " + e.getStatusCode(),
                        e
                );
            }

            throw e;
        }
    }
}
