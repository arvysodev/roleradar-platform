package com.roleradar.vacancy.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VacancyResponse(
        UUID id,
        String source,
        String externalId,
        String title,
        String companyName,
        String location,
        boolean remote,
        String url,
        String descriptionHtml,
        String descriptionText,
        LocalDateTime postedAt,
        LocalDateTime ingestedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String status,
        LocalDateTime lastSeenAt,
        LocalDateTime closedAt
) {
}
