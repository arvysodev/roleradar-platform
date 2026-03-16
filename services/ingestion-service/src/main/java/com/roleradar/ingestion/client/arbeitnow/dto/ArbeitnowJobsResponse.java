package com.roleradar.ingestion.client.arbeitnow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArbeitnowJobsResponse(
        List<ArbeitnowJobResponse> data,
        Links links,
        Meta meta
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Links(
            String first,
            String last,
            String prev,
            String next
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            Integer current_page,
            Integer per_page
    ) {
    }
}
