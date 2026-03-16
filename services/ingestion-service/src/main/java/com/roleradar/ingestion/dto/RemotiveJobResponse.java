package com.roleradar.ingestion.dto;

public record RemotiveJobResponse(
        Long id,
        String url,
        String title,
        String company_name,
        String candidate_required_location,
        String description,
        String publication_date
) {
}
