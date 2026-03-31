package com.roleradar.ingestion.integration;

import com.roleradar.ingestion.service.IngestionSchedulerService;
import com.roleradar.ingestion.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@TestPropertySource(properties = "roleradar.ingestion.scheduling.enabled=false")
class IngestionSchedulerServiceDisabledIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionSchedulerService ingestionSchedulerService;

    @MockitoBean
    private IngestionService ingestionService;

    @Test
    void ingestRemotiveOnSchedule_whenSchedulingDisabled_shouldNotInvokeIngestionService() {
        ingestionSchedulerService.ingestRemotiveOnSchedule();

        verify(ingestionService, never()).ingestRemotiveVacancies();
    }

    @Test
    void ingestArbeitnowOnSchedule_whenSchedulingDisabled_shouldNotInvokeIngestionService() {
        ingestionSchedulerService.ingestArbeitnowOnSchedule();

        verify(ingestionService, never()).ingestArbeitnowVacancies();
    }

    @Test
    void ingestAdzunaOnSchedule_whenSchedulingDisabled_shouldNotInvokeIngestionService() {
        ingestionSchedulerService.ingestAdzunaOnSchedule();

        verify(ingestionService, never()).ingestAdzunaVacancies();
    }
}
