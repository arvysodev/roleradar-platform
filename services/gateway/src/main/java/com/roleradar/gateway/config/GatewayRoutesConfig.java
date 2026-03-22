package com.roleradar.gateway.config;

import com.roleradar.gateway.dto.AuthResponse;
import com.roleradar.gateway.dto.AuthTokensGatewayResponse;
import com.roleradar.gateway.dto.LogoutRequest;
import com.roleradar.gateway.dto.RefreshRequest;
import com.roleradar.gateway.exception.UnauthorizedException;
import com.roleradar.gateway.security.AccessTokenCookieFactory;
import com.roleradar.gateway.security.CookieProperties;
import com.roleradar.gateway.security.RefreshTokenCookieFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           CookieProperties cookieProperties,
                                           AccessTokenCookieFactory accessTokenCookieFactory,
                                           RefreshTokenCookieFactory refreshTokenCookieFactory,
                                           ObjectMapper objectMapper) {
        return builder.routes()
                .route("auth-service-login", r -> r
                        .path("/api/v1/auth/login")
                        .filters(f -> f
                                .modifyResponseBody(
                                        String.class,
                                        String.class,
                                        MediaType.APPLICATION_JSON_VALUE,
                                        (exchange, body) -> rewriteAuthSuccessResponse(
                                                exchange.getResponse().getStatusCode(),
                                                body,
                                                objectMapper,
                                                accessTokenCookieFactory,
                                                refreshTokenCookieFactory,
                                                exchange
                                        )
                                )
                        )
                        .uri("http://auth-service:8081"))
                .route("auth-service-refresh", r -> r
                        .path("/api/v1/auth/refresh")
                        .filters(f -> f
                                .modifyRequestBody(
                                        String.class,
                                        RefreshRequest.class,
                                        MediaType.APPLICATION_JSON_VALUE,
                                        (exchange, body) -> Mono.just(
                                                new RefreshRequest(
                                                        extractRequiredCookie(
                                                                exchange.getRequest(),
                                                                cookieProperties.getRefreshTokenName()
                                                        )
                                                )
                                        )
                                )
                                .modifyResponseBody(
                                        String.class,
                                        String.class,
                                        MediaType.APPLICATION_JSON_VALUE,
                                        (exchange, body) -> rewriteAuthSuccessResponse(
                                                exchange.getResponse().getStatusCode(),
                                                body,
                                                objectMapper,
                                                accessTokenCookieFactory,
                                                refreshTokenCookieFactory,
                                                exchange
                                        )
                                )
                        )
                        .uri("http://auth-service:8081"))
                .route("auth-service-logout", r -> r
                        .path("/api/v1/auth/logout")
                        .filters(f -> f
                                .modifyRequestBody(
                                        String.class,
                                        LogoutRequest.class,
                                        MediaType.APPLICATION_JSON_VALUE,
                                        (exchange, body) -> Mono.just(
                                                new LogoutRequest(
                                                        extractRequiredCookie(
                                                                exchange.getRequest(),
                                                                cookieProperties.getRefreshTokenName()
                                                        )
                                                )
                                        )
                                )
                                .addResponseHeader("Set-Cookie", accessTokenCookieFactory.clear().toString())
                                .addResponseHeader("Set-Cookie", refreshTokenCookieFactory.clear().toString())
                        )
                        .uri("http://auth-service:8081"))
                .route("auth-service-other", r -> r
                        .path("/api/v1/auth/**")
                        .uri("http://auth-service:8081"))
                .route("auth-service-openapi", r -> r
                        .path("/aggregate/auth-service/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://auth-service:8081"))
                .route("vacancy-service", r -> r
                        .path("/api/v1/vacancies/**")
                        .uri("http://auth-service:8082"))
                .build();
    }

    private static String extractRequiredCookie(ServerHttpRequest request, String cookieName) {
        var cookie = request.getCookies().getFirst(cookieName);

        if (cookie == null || cookie.getValue().isBlank()) {
            throw new UnauthorizedException("Refresh token cookie is missing.");
        }

        return cookie.getValue();
    }

    private static Mono<String> rewriteAuthSuccessResponse(
            HttpStatusCode statusCode,
            String body,
            ObjectMapper objectMapper,
            AccessTokenCookieFactory accessTokenCookieFactory,
            RefreshTokenCookieFactory refreshTokenCookieFactory,
            org.springframework.web.server.ServerWebExchange exchange
    ) {
        if (statusCode == null || !statusCode.is2xxSuccessful() || body == null || body.isBlank()) {
            return Mono.justOrEmpty(body);
        }

        try {
            AuthTokensGatewayResponse tokens =
                    objectMapper.readValue(body, AuthTokensGatewayResponse.class);

            exchange.getResponse().addCookie(
                    accessTokenCookieFactory.create(
                            tokens.accessToken(),
                            tokens.accessTokenExpiresInSeconds()
                    )
            );

            exchange.getResponse().addCookie(
                    refreshTokenCookieFactory.create(
                            tokens.refreshToken(),
                            tokens.refreshTokenExpiresInSeconds()
                    )
            );

            AuthResponse response = new AuthResponse(
                    tokens.tokenType(),
                    tokens.accessTokenExpiresInSeconds()
            );

            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }
    }
}
