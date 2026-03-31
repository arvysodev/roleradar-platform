package com.roleradar.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAudienceValidatorTest {

    @Test
    void validate_whenAudienceContainsExpectedAudience_shouldReturnSuccess() {
        JwtAudienceValidator validator = new JwtAudienceValidator("roleradar-api");

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "user-123",
                        "aud", List.of("roleradar-api")
                )
        );

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void validate_whenAudienceDoesNotContainExpectedAudience_shouldReturnFailure() {
        JwtAudienceValidator validator = new JwtAudienceValidator("roleradar-api");

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "user-123",
                        "aud", List.of("some-other-audience")
                )
        );

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getErrorCode()).isEqualTo("invalid_token");
            assertThat(error.getDescription()).isEqualTo("The required audience is missing.");
        });
    }
}
