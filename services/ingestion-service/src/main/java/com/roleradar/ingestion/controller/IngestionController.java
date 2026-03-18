package com.roleradar.ingestion.controller;

import com.roleradar.ingestion.dto.IngestionRunResult;
import com.roleradar.ingestion.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/remotive")
    public ResponseEntity<IngestionRunResult> ingestRemotiveVacancies() {
        IngestionRunResult result = ingestionService.ingestRemotiveVacancies();
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping("/arbeitnow")
    public ResponseEntity<IngestionRunResult> ingestArbeitnowVacancies() {
        IngestionRunResult result = ingestionService.ingestArbeitnowVacancies();
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping("/adzuna")
    public ResponseEntity<IngestionRunResult> ingestAdzunaVacancies() {
        IngestionRunResult result = ingestionService.ingestAdzunaVacancies();
        return ResponseEntity.accepted().body(result);
    }
}
