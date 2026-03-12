package com.roleradar.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username contains invalid characters."
        )
        String username,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}