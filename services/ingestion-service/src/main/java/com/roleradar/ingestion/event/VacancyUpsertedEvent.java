package com.roleradar.ingestion.event;

import java.time.LocalDateTime;

public record VacancyUpsertedEvent(
        String source,
        String externalId,
        String title,
        String companyName,
        String location,
        boolean remote,
        String url,
        String description,
        LocalDateTime postedAt,
        LocalDateTime ingestedAt
) {
}
