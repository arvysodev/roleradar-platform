package com.roleradar.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenCookieServerAuthenticationConverterTest {

    @Test
    void convert_whenAccessTokenCookieExists_shouldReturnBearerTokenAuthentication() {
        AccessTokenCookieServerAuthenticationConverter converter =
                new AccessTokenCookieServerAuthenticationConverter("access_token");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/vacancies")
                .cookie(new HttpCookie("access_token", "cookie-token-123"))
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        var authentication = converter.convert(exchange).block();

        assertThat(authentication).isInstanceOf(BearerTokenAuthenticationToken.class);

        BearerTokenAuthenticationToken bearer =
                (BearerTokenAuthenticationToken) authentication;

        assertThat(bearer.getToken()).isEqualTo("cookie-token-123");
    }

    @Test
    void convert_whenAccessTokenCookieIsMissing_shouldReturnNull() {
        AccessTokenCookieServerAuthenticationConverter converter =
                new AccessTokenCookieServerAuthenticationConverter("access_token");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/vacancies").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        var authentication = converter.convert(exchange).block();

        assertThat(authentication).isNull();
    }

    @Test
    void convert_whenAccessTokenCookieIsBlank_shouldFallBackToAuthorizationHeader() {
        AccessTokenCookieServerAuthenticationConverter converter =
                new AccessTokenCookieServerAuthenticationConverter("access_token");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/vacancies")
                .cookie(new HttpCookie("access_token", ""))
                .header("Authorization", "Bearer header-token-456")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        var authentication = converter.convert(exchange).block();

        assertThat(authentication).isInstanceOf(BearerTokenAuthenticationToken.class);

        BearerTokenAuthenticationToken bearer =
                (BearerTokenAuthenticationToken) authentication;

        assertThat(bearer.getToken()).isEqualTo("header-token-456");
    }
}
