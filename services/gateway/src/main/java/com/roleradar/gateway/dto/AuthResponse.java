package com.roleradar.gateway.dto;

public record AuthResponse(
        String tokenType,
        long expiresIn
) {
}
