package com.roleradar.auth.dto;

public record AuthTokensResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken
) {
}