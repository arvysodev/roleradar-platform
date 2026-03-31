package com.roleradar.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ForwardBearerTokenGatewayFilterTest {

    @Test
    void filter_whenAuthenticatedJwtExists_shouldAddAuthorizationHeader() {
        ForwardBearerTokenGatewayFilter filter = new ForwardBearerTokenGatewayFilter();

        Jwt jwt = new Jwt(
                "jwt-token-123",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of(
                        "sub", "user-123",
                        "roles", List.of("USER")
                )
        );

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject());

        MockServerWebExchange baseExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vacancies").build()
        );

        ServerWebExchange exchange = new ServerWebExchangeDecorator(baseExchange) {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends java.security.Principal> Mono<T> getPrincipal() {
                return (Mono<T>) Mono.just(authentication);
            }
        };

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();

        GatewayFilterChain chain = gatewayExchange -> {
            capturedExchange.set(gatewayExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get()).isNotNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer jwt-token-123");
    }

    @Test
    void filter_whenPrincipalIsMissing_shouldNotAddAuthorizationHeader() {
        ForwardBearerTokenGatewayFilter filter = new ForwardBearerTokenGatewayFilter();

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vacancies").build()
        );

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();

        GatewayFilterChain chain = gatewayExchange -> {
            capturedExchange.set(gatewayExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get()).isNotNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION))
                .isFalse();
    }
}
