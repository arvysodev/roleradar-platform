package com.roleradar.ingestion.service;

import com.roleradar.ingestion.dto.IngestionRunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IngestionSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(IngestionSchedulerService.class);

    private final IngestionService ingestionService;
    private final boolean schedulingEnabled;

    public IngestionSchedulerService(
            IngestionService ingestionService,
            @Value("${roleradar.ingestion.scheduling.enabled:true}") boolean schedulingEnabled
            ) {
        this.ingestionService = ingestionService;
        this.schedulingEnabled = schedulingEnabled;
    }

    @Scheduled(cron = "${roleradar.ingestion.scheduling.remotive-cron}")
    public void ingestRemotiveOnSchedule() {
        if (!schedulingEnabled) return;

        runScheduledIngestion("REMOTIVE", ingestionService::ingestRemotiveVacancies);
    }

    @Scheduled(cron = "${roleradar.ingestion.scheduling.arbeitnow-cron}")
    public void ingestArbeitnowOnSchedule() {
        if (!schedulingEnabled) {
            return;
        }

        runScheduledIngestion("ARBEITNOW", ingestionService::ingestArbeitnowVacancies);
    }

    @Scheduled(cron = "${roleradar.ingestion.scheduling.adzuna-cron}")
    public void ingestAdzunaOnSchedule() {
        if (!schedulingEnabled) {
            return;
        }

        runScheduledIngestion("ADZUNA", ingestionService::ingestAdzunaVacancies);
    }

    public void runScheduledIngestion(String source, ScheduledIngestionAction action) {
        try {
            log.info("Starting scheduled ingestion: source={}", source);

            IngestionRunResult result = action.run();

            log.info(
                    "Finished scheduled ingestion: source={} fetched={} published={}",
                    result.source(),
                    result.fetched(),
                    result.published()
            );
        } catch (Exception e) {
            log.error("Scheduled ingestion failed: source={}", source, e);
        }
    }

    @FunctionalInterface
    public interface ScheduledIngestionAction {
        IngestionRunResult run();
    }
}
