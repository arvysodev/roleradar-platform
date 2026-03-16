package com.roleradar.ingestion.client.arbeitnow.dto;

import java.util.List;

public record ArbeitnowJobResponse(
        String slug,
        String company_name,
        String title,
        String description,
        boolean remote,
        String url,
        List<String> tags,
        List<String> job_types,
        String location,
        Long created_at
) {
}
