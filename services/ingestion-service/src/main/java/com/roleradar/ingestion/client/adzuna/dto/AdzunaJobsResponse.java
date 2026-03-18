package com.roleradar.ingestion.client.adzuna.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdzunaJobsResponse(
        List<AdzunaJobResponse> results
) {
}
