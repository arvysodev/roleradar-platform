package com.roleradar.ingestion.client.adzuna.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdzunaJobResponse(
        String id,
        String title,
        String description,
        String created,
        String redirect_url,
        Company company,
        Location location,
        Category category,
        Integer salary_min,
        Integer salary_max,
        String contract_type,
        String contract_time
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(
            String display_name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            String display_name,
            List<String> area
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Category(
            String label,
            String tag
    ) {
    }
}
