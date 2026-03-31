package com.roleradar.gateway.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Import(GatewaySecurityIntegrationTest.TestEndpointsConfiguration.class)
class GatewaySecurityIntegrationTest {

    private final ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    GatewaySecurityIntegrationTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("security.jwt.issuer", () -> "http://auth-service:8081");
        registry.add("security.jwt.audience", () -> "roleradar-api");
        registry.add("security.jwt.jwk-set-uri", () -> "http://auth-service:8081/.well-known/jwks.json");

        registry.add("security.cookies.access-token-name", () -> "access_token");
        registry.add("security.cookies.refresh-token-name", () -> "refresh_token");
        registry.add("security.cookies.secure", () -> "false");
        registry.add("security.cookies.same-site", () -> "Lax");
    }

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void protectedEndpoint_whenUnauthenticated_shouldReturnUnauthorizedProblemDetail() {
        webTestClient.get()
                .uri("/test/protected")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Unauthorized")
                .jsonPath("$.detail").isEqualTo("Authentication is required.")
                .jsonPath("$.type").isEqualTo("https://roleradar.app/problems/unauthorized")
                .jsonPath("$.instance").isEqualTo("/test/protected");
    }

    @Test
    void csrfEndpoint_shouldBePublicAndReturnToken() {
        webTestClient.get()
                .uri("/csrf")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*XSRF-TOKEN=.*")
                .expectBody()
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    void optionsRequest_shouldBePermittedBySecurityConfiguration() {
        webTestClient.options()
                .uri("/test/protected")
                .exchange()
                .expectStatus().isOk();
    }

    @TestConfiguration
    static class TestEndpointsConfiguration {

        @Bean
        TestProtectedController testProtectedController() {
            return new TestProtectedController();
        }
    }

    @RestController
    static class TestProtectedController {

        @GetMapping("/test/protected")
        Map<String, String> protectedEndpoint() {
            return Map.of("status", "ok");
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/test/admin")
        Map<String, String> adminEndpoint() {
            return Map.of("status", "ok");
        }
    }
}
