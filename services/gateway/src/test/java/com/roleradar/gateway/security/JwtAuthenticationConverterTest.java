package com.roleradar.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    @Test
    void convert_whenRolesClaimExists_shouldMapRolesToAuthorities() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of(
                        "sub", "user-123",
                        "roles", List.of("USER", "ADMIN")
                )
        );

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user-123");
        assertThat(authentication.getAuthorities())
                .containsExactlyInAnyOrder(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
    }

    @Test
    void convert_whenRoleAlreadyHasPrefix_shouldPreserveIt() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of(
                        "sub", "user-123",
                        "roles", List.of("ROLE_USER")
                )
        );

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .containsExactly(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void convert_whenRolesClaimIsMissing_shouldReturnAuthenticationWithoutAuthorities() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", "user-123")
        );

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user-123");
        assertThat(authentication.getAuthorities()).isEmpty();
    }
}
