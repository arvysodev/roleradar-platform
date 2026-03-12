package com.roleradar.auth.service;

public record AuthTokens(
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken
) {
}