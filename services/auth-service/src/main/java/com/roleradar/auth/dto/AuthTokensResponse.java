package com.roleradar.auth.dto;

public record AuthTokensResponse(
        String tokenType,
        long accessTokenExpiresInSeconds
) {
}