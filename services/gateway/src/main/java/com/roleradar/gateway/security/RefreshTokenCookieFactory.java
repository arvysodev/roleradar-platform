package com.roleradar.gateway.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    private final CookieProperties cookieProperties;

    public RefreshTokenCookieFactory(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public ResponseCookie create(String token, long maxAgeSeconds) {
        return ResponseCookie.from(cookieProperties.getRefreshTokenName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/api/v1/auth/refresh")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(cookieProperties.getRefreshTokenName(), "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .build();
    }
}
