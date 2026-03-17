package com.roleradar.ingestion.mapper;

import com.roleradar.ingestion.client.arbeitnow.dto.ArbeitnowJobResponse;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", uses = HtmlDescriptionMapperSupport.class, imports = LocalDateTime.class)
public interface ArbeitnowVacancyMapper {

    @Mapping(target = "eventId", expression = "java(null)")
    @Mapping(target = "source", constant = "ARBEITNOW")
    @Mapping(target = "externalId", source = "slug")
    @Mapping(target = "companyName", source = "company_name")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "remote", source = "remote")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "descriptionHtml", source = "description")
    @Mapping(target = "descriptionText", source = "description", qualifiedByName = "htmlToPlainText")
    @Mapping(target = "postedAt", expression = "java(parseCreatedAt(job.created_at()))")
    @Mapping(target = "ingestedAt", expression = "java(LocalDateTime.now())")
    VacancyUpsertedEvent toEvent(ArbeitnowJobResponse job);

    default LocalDateTime parseCreatedAt(Long createdAt) {
        if (createdAt == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(createdAt),
                ZoneOffset.UTC
        );
    }
}
