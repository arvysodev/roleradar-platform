package com.roleradar.ingestion.dto;

import java.util.List;

public record RemotiveJobsResponse(
        List<RemotiveJobResponse> jobs
) {
}
