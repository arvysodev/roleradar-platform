package com.roleradar.vacancy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.roleradar.vacancy.domain.event.ProcessedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    long deleteByProcessedAtBefore(LocalDateTime cutoff);
}
