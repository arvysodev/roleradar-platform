package com.roleradar.gateway.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutesIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static HttpServer authServiceServer;
    private static HttpServer vacancyServiceServer;

    private static final AtomicReference<String> lastRefreshRequestBody = new AtomicReference<>();
    private static final AtomicReference<String> lastLogoutRequestBody = new AtomicReference<>();
    private static final AtomicReference<String> lastVacancyAuthorizationHeader = new AtomicReference<>();

    private final ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    GatewayRoutesIntegrationTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        startMockServersIfNeeded();

        registry.add("security.jwt.issuer", () -> "http://auth-service:8081");
        registry.add("security.jwt.audience", () -> "roleradar-api");
        registry.add("security.jwt.jwk-set-uri", () -> authServiceBaseUrl() + "/.well-known/jwks.json");

        registry.add("security.cookies.access-token-name", () -> "access_token");
        registry.add("security.cookies.refresh-token-name", () -> "refresh_token");
        registry.add("security.cookies.secure", () -> "false");
        registry.add("security.cookies.same-site", () -> "Lax");

        registry.add("gateway.routes.auth-service-uri", GatewayRoutesIntegrationTest::authServiceBaseUrl);
        registry.add("gateway.routes.vacancy-service-uri", GatewayRoutesIntegrationTest::vacancyServiceBaseUrl);
    }

    @BeforeEach
    void setUp() {
        lastRefreshRequestBody.set(null);
        lastLogoutRequestBody.set(null);
        lastVacancyAuthorizationHeader.set(null);

        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @AfterAll
    static void tearDown() {
        if (authServiceServer != null) {
            authServiceServer.stop(0);
        }

        if (vacancyServiceServer != null) {
            vacancyServiceServer.stop(0);
        }
    }

    @Test
    void login_shouldRewriteResponseBodyAndSetAuthCookies() {
        EntityExchangeResult<byte[]> result = webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "email": "artem@example.com",
                          "password": "Password123"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresIn").isEqualTo(900)
                .jsonPath("$.accessToken").doesNotExist()
                .jsonPath("$.refreshToken").doesNotExist()
                .returnResult();

        ResponseCookie accessCookie = result.getResponseCookies().getFirst("access_token");
        ResponseCookie refreshCookie = result.getResponseCookies().getFirst("refresh_token");

        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getValue()).isEqualTo("access-token-1");
        assertThat(accessCookie.getPath()).isEqualTo("/");
        assertThat(accessCookie.isHttpOnly()).isTrue();

        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("refresh-token-1");
        assertThat(refreshCookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(refreshCookie.isHttpOnly()).isTrue();
    }

    @Test
    void refresh_shouldReadRefreshTokenFromCookieRewriteRequestBodyAndSetNewCookies() throws Exception {
        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/refresh")
                .cookie("refresh_token", "refresh-cookie-value")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "refreshToken": "client-body-should-be-ignored"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresIn").isEqualTo(901)
                .jsonPath("$.accessToken").doesNotExist()
                .jsonPath("$.refreshToken").doesNotExist();

        JsonNode refreshBody = OBJECT_MAPPER.readTree(lastRefreshRequestBody.get());

        assertThat(refreshBody.get("refreshToken").asText()).isEqualTo("refresh-cookie-value");

        EntityExchangeResult<byte[]> result = webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/refresh")
                .cookie("refresh_token", "refresh-cookie-value")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "refreshToken": "ignored-again"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult();

        ResponseCookie accessCookie = result.getResponseCookies().getFirst("access_token");
        ResponseCookie refreshCookie = result.getResponseCookies().getFirst("refresh_token");

        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getValue()).isEqualTo("access-token-2");

        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("refresh-token-2");
    }

    @Test
    void refresh_whenRefreshCookieIsMissing_shouldReturnUnauthorizedProblemDetail() {
        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "refreshToken": "body-only-token"
                        }
                        """)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Unauthorized")
                .jsonPath("$.detail").isEqualTo("Refresh token cookie is missing.")
                .jsonPath("$.type").isEqualTo("https://roleradar.app/problems/unauthorized")
                .jsonPath("$.instance").isEqualTo("/api/v1/auth/refresh");
    }

    @Test
    void logout_shouldReadRefreshTokenFromCookieRewriteRequestBodyAndClearCookies() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/logout")
                .cookie("refresh_token", "refresh-cookie-value")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "refreshToken": "client-body-should-be-ignored"
                        }
                        """)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .returnResult();

        JsonNode logoutBody = OBJECT_MAPPER.readTree(lastLogoutRequestBody.get());

        assertThat(logoutBody.get("refreshToken").asText()).isEqualTo("refresh-cookie-value");

        List<String> setCookieHeaders = result.getResponseHeaders().get(HttpHeaders.SET_COOKIE);

        assertThat(setCookieHeaders).isNotNull();
        assertThat(setCookieHeaders).hasSizeGreaterThanOrEqualTo(2);

        assertThat(setCookieHeaders).anyMatch(header ->
                header.startsWith("access_token=")
                        && header.contains("Max-Age=0")
                        && header.contains("Path=/"));

        assertThat(setCookieHeaders).anyMatch(header ->
                header.startsWith("refresh_token=")
                        && header.contains("Max-Age=0")
                        && header.contains("Path=/api/v1/auth"));
    }

    @Test
    void authenticatedVacancyRequest_shouldForwardBearerTokenToDownstreamService() {
        webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .tokenValue("test-access-token")
                        .claim("roles", List.of("USER"))))
                .get()
                .uri("/api/v1/vacancies")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");

        assertThat(lastVacancyAuthorizationHeader.get()).isEqualTo("Bearer test-access-token");
    }

    private static void startMockServersIfNeeded() {
        if (authServiceServer != null && vacancyServiceServer != null) {
            return;
        }

        try {
            authServiceServer = HttpServer.create(new InetSocketAddress(0), 0);
            vacancyServiceServer = HttpServer.create(new InetSocketAddress(0), 0);

            authServiceServer.createContext("/.well-known/jwks.json", exchange ->
                    writeJson(exchange, HttpStatus.OK.value(), """
                            { "keys": [] }
                            """));

            authServiceServer.createContext("/api/v1/auth/login", exchange ->
                    writeJson(exchange, HttpStatus.OK.value(), """
                            {
                              "tokenType": "Bearer",
                              "accessToken": "access-token-1",
                              "accessTokenExpiresInSeconds": 900,
                              "refreshToken": "refresh-token-1",
                              "refreshTokenExpiresInSeconds": 604800
                            }
                            """));

            authServiceServer.createContext("/api/v1/auth/refresh", exchange -> {
                lastRefreshRequestBody.set(readBody(exchange));
                writeJson(exchange, HttpStatus.OK.value(), """
                        {
                          "tokenType": "Bearer",
                          "accessToken": "access-token-2",
                          "accessTokenExpiresInSeconds": 901,
                          "refreshToken": "refresh-token-2",
                          "refreshTokenExpiresInSeconds": 604801
                        }
                        """);
            });

            authServiceServer.createContext("/api/v1/auth/logout", exchange -> {
                lastLogoutRequestBody.set(readBody(exchange));
                exchange.sendResponseHeaders(HttpStatus.NO_CONTENT.value(), -1);
                exchange.close();
            });

            vacancyServiceServer.createContext("/api/v1/vacancies", exchange -> {
                lastVacancyAuthorizationHeader.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                writeJson(exchange, HttpStatus.OK.value(), """
                        {
                          "status": "ok"
                        }
                        """);
            });

            authServiceServer.start();
            vacancyServiceServer.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start mock downstream servers for gateway tests.", ex);
        }
    }

    private static String authServiceBaseUrl() {
        return "http://localhost:" + authServiceServer.getAddress().getPort();
    }

    private static String vacancyServiceBaseUrl() {
        return "http://localhost:" + vacancyServiceServer.getAddress().getPort();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        exchange.sendResponseHeaders(status, responseBody.length);
        exchange.getResponseBody().write(responseBody);
        exchange.close();
    }
}
