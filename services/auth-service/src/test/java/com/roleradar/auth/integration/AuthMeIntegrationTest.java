package com.roleradar.auth.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthMeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void me_whenAuthenticatedWithUserRole_shouldReturnCurrentUserClaims() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("8d6cc0b4-43f1-4f6e-8cd2-6f7d4b9d6c11")
                                        .claim("email", "artem@example.com")
                                        .claim("preferred_username", "artem")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("8d6cc0b4-43f1-4f6e-8cd2-6f7d4b9d6c11"))
                .andExpect(jsonPath("$.email").value("artem@example.com"))
                .andExpect(jsonPath("$.username").value("artem"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void me_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_whenAuthenticatedWithoutAllowedRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("8d6cc0b4-43f1-4f6e-8cd2-6f7d4b9d6c11")
                                .claim("email", "artem@example.com")
                                .claim("preferred_username", "artem")
                                .claim("roles", List.of("GUEST"))
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void me_whenAuthenticatedWithAdminRole_shouldReturnCurrentUserClaims() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("c6cb5e54-0640-489a-a31f-4c3ee7c1dca3")
                                        .claim("email", "admin@example.com")
                                        .claim("preferred_username", "admin")
                                        .claim("roles", List.of("ADMIN"))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("c6cb5e54-0640-489a-a31f-4c3ee7c1dca3"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }
}
