package com.roleradar.ingestion.dto;

public record IngestionRunResult(
        String source,
        int fetched,
        int published
) {
}
