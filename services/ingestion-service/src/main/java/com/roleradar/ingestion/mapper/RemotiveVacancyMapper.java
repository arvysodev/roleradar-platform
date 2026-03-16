package com.roleradar.ingestion.mapper;

import com.roleradar.ingestion.dto.RemotiveJobResponse;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
public class RemotiveVacancyMapper {

    public VacancyUpsertedEvent toEvent(RemotiveJobResponse job) {
        return new VacancyUpsertedEvent(
                "REMOTIVE",
                String.valueOf(job.id()),
                job.title(),
                job.company_name(),
                job.candidate_required_location(),
                true,
                job.url(),
                job.description(),
                parsePostedAt(job.publication_date()),
                LocalDateTime.now()
        );
    }

    private LocalDateTime parsePostedAt(String publicationDate) {
        if (publicationDate == null || publicationDate.isBlank()) {
            return null;
        }

        return OffsetDateTime.parse(publicationDate).toLocalDateTime();
    }
}
