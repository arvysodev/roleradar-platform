package com.roleradar.ingestion.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("roleradar.ingestion.remotive.max-jobs-per-run", () -> "3");
        registry.add("roleradar.ingestion.arbeitnow.max-jobs-per-run", () -> "3");
        registry.add("roleradar.ingestion.arbeitnow.max-pages-per-run", () -> "2");
        registry.add("roleradar.ingestion.scheduling.enabled", () -> "true");
        registry.add("roleradar.ingestion.scheduling.remotive-cron", () -> "0 */5 * * * *");
        registry.add("roleradar.ingestion.scheduling.arbeitnow-cron", () -> "0 */10 * * * *");
        registry.add("roleradar.ingestion.scheduling.adzuna-cron", () -> "0 */15 * * * *");

        registry.add("roleradar.ingestion.adzuna.max-jobs-per-run", () -> "3");
        registry.add("roleradar.ingestion.adzuna.max-pages-per-run", () -> "2");
        registry.add("roleradar.ingestion.adzuna.results-per-page", () -> "10");
        registry.add("roleradar.ingestion.adzuna.app-id", () -> "test-app-id");
        registry.add("roleradar.ingestion.adzuna.app-key", () -> "test-app-key");
        registry.add("roleradar.ingestion.adzuna.countries", () -> "gb,pl");

        registry.add("roleradar.ingestion.topics.vacancy-upserted", () -> "vacancy-upserted");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");

        registry.add("roleradar.ingestion.http.retry.max-retries", () -> "2");
        registry.add("roleradar.ingestion.http.retry.delay-millis", () -> "100");
    }
}
