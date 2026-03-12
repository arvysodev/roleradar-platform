package com.roleradar.auth.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenCookieFactory {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    public ResponseCookie createAccessTokenCookie(String accessToken, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie createDeleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
