package com.roleradar.gateway.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenCookieFactory {

    private final CookieProperties cookieProperties;

    public AccessTokenCookieFactory(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public ResponseCookie create(String token, long maxAgeSeconds) {
        return ResponseCookie.from(cookieProperties.getAccessTokenName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(cookieProperties.getAccessTokenName(), "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(0)
                .build();
    }
}
