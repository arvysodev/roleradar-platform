package com.roleradar.gateway.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AccessTokenCookieServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final String accessTokenCookieName;
    private final ServerBearerTokenAuthenticationConverter defaultConverter =
            new ServerBearerTokenAuthenticationConverter();

    public AccessTokenCookieServerAuthenticationConverter(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        HttpCookie accessTokenCookie = exchange.getRequest().getCookies().getFirst(accessTokenCookieName);

        if (accessTokenCookie != null && !accessTokenCookie.getValue().isBlank()) {
            return Mono.just(new BearerTokenAuthenticationToken(accessTokenCookie.getValue()));
        }

        return defaultConverter.convert(exchange)
                .cast(Authentication.class);
    }
}
