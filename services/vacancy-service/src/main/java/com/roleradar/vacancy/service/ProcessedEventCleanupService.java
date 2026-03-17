package com.roleradar.vacancy.service;

import com.roleradar.vacancy.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProcessedEventCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedEventCleanupService.class);

    private final ProcessedEventRepository processedEventRepository;
    private final long retentionDays;

    public ProcessedEventCleanupService(
            ProcessedEventRepository processedEventRepository,
            @Value("${roleradar.cleanup.processed-events.retention-days}") long retentionDays
    ) {
        this.processedEventRepository = processedEventRepository;
        this.retentionDays = retentionDays;
    }

    @Transactional
    @Scheduled(cron = "${roleradar.cleanup.processed-events.cron}")
    public void deleteOldProcessedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        long deleted = processedEventRepository.deleteByProcessedAtBefore(cutoff);

        if (deleted > 0) {
            log.info(
                    "Deleted old processed events: count={} cutoff={}",
                    deleted,
                    cutoff
            );
        }
    }
}
