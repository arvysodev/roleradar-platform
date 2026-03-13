package com.roleradar.auth.dto;

import java.util.List;

public record MeResponse(
        String id,
        String email,
        String username,
        List<String> roles
) {
}