package com.roleradar.ingestion.mapper;

import com.roleradar.ingestion.client.adzuna.dto.AdzunaJobResponse;
import com.roleradar.ingestion.event.VacancyUpsertedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", uses = HtmlDescriptionMapperSupport.class, imports = LocalDateTime.class)
public interface AdzunaVacancyMapper {

    @Mapping(target = "eventId", expression = "java(null)")
    @Mapping(target = "source", constant = "ADZUNA")
    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "companyName", source = "company.display_name")
    @Mapping(target = "location", source = "location.display_name")
    @Mapping(target = "remote", expression = "java(detectRemote(job))")
    @Mapping(target = "url", source = "redirect_url")
    @Mapping(target = "descriptionHtml", source = "description", qualifiedByName = "plainTextToHtml")
    @Mapping(target = "descriptionText", source = "description")
    @Mapping(target = "postedAt", expression = "java(parseCreatedAt(job.created()))")
    @Mapping(target = "ingestedAt", expression = "java(LocalDateTime.now())")
    VacancyUpsertedEvent toEvent(AdzunaJobResponse job);

    default LocalDateTime parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            return null;
        }

        return OffsetDateTime.parse(createdAt).toLocalDateTime();
    }

    default boolean detectRemote(AdzunaJobResponse job) {
        String title = job.title() == null ? "" : job.title();
        String description = job.description() == null ? "" : job.description();
        String location = job.location() == null || job.location().display_name() == null
                ? ""
                : job.location().display_name();

        String combined = (title + " " + description + " " + location).toLowerCase();

        return combined.contains("remote")
                || combined.contains("hybrid")
                || combined.contains("work from home")
                || combined.contains("home office");
    }
}
