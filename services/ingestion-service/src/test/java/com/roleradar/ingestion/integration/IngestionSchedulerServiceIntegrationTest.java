package com.roleradar.ingestion.integration;

import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.service.IngestionSchedulerService;
import com.roleradar.ingestion.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionSchedulerServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionSchedulerService ingestionSchedulerService;

    @MockitoBean
    private IngestionService ingestionService;

    @Test
    void ingestRemotiveOnSchedule_whenSchedulingEnabled_shouldInvokeIngestionService() {
        when(ingestionService.ingestRemotiveVacancies())
                .thenReturn(new IngestionRunResult("REMOTIVE", 10, 3));

        ingestionSchedulerService.ingestRemotiveOnSchedule();

        verify(ingestionService, times(1)).ingestRemotiveVacancies();
    }

    @Test
    void ingestArbeitnowOnSchedule_whenSchedulingEnabled_shouldInvokeIngestionService() {
        when(ingestionService.ingestArbeitnowVacancies())
                .thenReturn(new IngestionRunResult("ARBEITNOW", 20, 5));

        ingestionSchedulerService.ingestArbeitnowOnSchedule();

        verify(ingestionService, times(1)).ingestArbeitnowVacancies();
    }

    @Test
    void ingestAdzunaOnSchedule_whenSchedulingEnabled_shouldInvokeIngestionService() {
        when(ingestionService.ingestAdzunaVacancies())
                .thenReturn(new IngestionRunResult("ADZUNA", 30, 7));

        ingestionSchedulerService.ingestAdzunaOnSchedule();

        verify(ingestionService, times(1)).ingestAdzunaVacancies();
    }

    @Test
    void runScheduledIngestion_whenActionThrows_shouldSwallowException() {
        ingestionSchedulerService.runScheduledIngestion("REMOTIVE", () -> {
            throw new IllegalStateException("boom");
        });
    }
}
