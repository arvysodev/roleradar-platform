package com.roleradar.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String issuer,
        String audience,
        String jwkSetUri,
        String accessTokenCookieName
) {
}