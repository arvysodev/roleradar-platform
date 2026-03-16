package com.roleradar.ingestion.mapper;

import com.roleradar.ingestion.dto.RemotiveJobResponse;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface RemotiveVacancyMapper {

    @Mapping(target = "source", constant = "REMOTIVE")
    @Mapping(target = "externalId", expression = "java(String.valueOf(job.id()))")
    @Mapping(target = "companyName", source = "company_name")
    @Mapping(target = "location", source = "candidate_required_location")
    @Mapping(target = "remote", constant = "true")
    @Mapping(target = "postedAt", expression = "java(parsePostedAt(job.publication_date()))")
    @Mapping(target = "ingestedAt", expression = "java(LocalDateTime.now())")
    VacancyUpsertedEvent toEvent(RemotiveJobResponse job);

    default LocalDateTime parsePostedAt(String publicationDate) {
        if (publicationDate == null || publicationDate.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(publicationDate).toLocalDateTime();
    }
}
