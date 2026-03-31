package com.roleradar.vacancy.integration;

import com.roleradar.vacancy.repository.ProcessedEventRepository;
import com.roleradar.vacancy.repository.VacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.9")
            .withDatabaseName("vacancy_test_db")
            .withUsername("vacancy_user")
            .withPassword("vacancy_password");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected VacancyRepository vacancyRepository;

    @Autowired
    protected ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void cleanDatabase() {
        processedEventRepository.deleteAllInBatch();
        vacancyRepository.deleteAllInBatch();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("security.jwt.issuer", () -> "http://localhost:8081");
        registry.add("security.jwt.audience", () -> "roleradar-api");
        registry.add("security.jwt.jwk-set-uri", () -> "http://localhost:8081/.well-known/jwks.json");

        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }
}
