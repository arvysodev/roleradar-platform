package com.roleradar.auth.dto;

public record UserResponse(
        Long id,
        String email,
        String role
) {
}