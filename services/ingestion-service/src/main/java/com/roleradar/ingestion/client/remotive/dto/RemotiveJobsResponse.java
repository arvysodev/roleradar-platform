package com.roleradar.ingestion.client.remotive.dto;

import java.util.List;

public record RemotiveJobsResponse(
        List<RemotiveJobResponse> jobs
) {
}
