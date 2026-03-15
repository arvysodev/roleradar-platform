package com.roleradar.vacancy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "vacancies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vacancies_source_external_id", columnNames = {"source", "external_id"})
        }
)
public class Vacancy {

    public Vacancy(VacancySource source,
                   String externalId,
                   String title,
                   String companyName,
                   String location,
                   boolean remote,
                   String url,
                   String description,
                   LocalDateTime postedAt,
                   LocalDateTime ingestedAt) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.remote = remote;
        this.url = url;
        this.description = description;
        this.postedAt = postedAt;
        this.ingestedAt = ingestedAt;
        this.status = VacancyStatus.ACTIVE;
    }

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VacancySource source;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(length = 255)
    private String location;

    @Column(nullable = false)
    private boolean remote;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false)
    private String description;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VacancyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markClosed() {
        this.status = VacancyStatus.CLOSED;
    }

    public void markActive() {
        this.status = VacancyStatus.ACTIVE;
    }

    public void refreshFromSource(String title,
                                  String companyName,
                                  String location,
                                  boolean remote,
                                  String url,
                                  String description,
                                  LocalDateTime postedAt,
                                  LocalDateTime ingestedAt) {
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.remote = remote;
        this.url = url;
        this.description = description;
        this.postedAt = postedAt;
        this.ingestedAt = ingestedAt;
        this.status = VacancyStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (ingestedAt == null) ingestedAt = now;
        if (status == null) status = VacancyStatus.ACTIVE;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
