package com.roleradar.auth.integration;

import com.jayway.jsonpath.JsonPath;
import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.repository.RefreshTokenRepository;
import com.roleradar.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLogoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void logout_shouldRevokeRefreshTokenAndReturnNoContent() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.saveAndFlush(user);

        String loginRequestBody = """
                {
                  "email": "artem@example.com",
                  "password": "Password123"
                }
                """;

        String loginResponseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = JsonPath.read(loginResponseBody, "$.refreshToken");

        RefreshToken storedTokenBeforeLogout = refreshTokenRepository.findAll().getFirst();
        assertThat(storedTokenBeforeLogout.getRevokedAt()).isNull();

        String logoutRequestBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutRequestBody))
                .andExpect(status().isNoContent());

        RefreshToken storedTokenAfterLogout = refreshTokenRepository.findById(storedTokenBeforeLogout.getId())
                .orElseThrow();

        assertThat(storedTokenAfterLogout.getRevokedAt()).isNotNull();
    }

    @Test
    void logout_whenRefreshTokenDoesNotExist_shouldStillReturnNoContent() throws Exception {
        String requestBody = """
                {
                  "refreshToken": "non-existent-refresh-token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.count()).isZero();
    }

    @Test
    void logout_whenRefreshTokenIsAlreadyRevoked_shouldStillReturnNoContent() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.saveAndFlush(user);

        String loginRequestBody = """
                {
                  "email": "artem@example.com",
                  "password": "Password123"
                }
                """;

        String loginResponseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = JsonPath.read(loginResponseBody, "$.refreshToken");

        RefreshToken storedToken = refreshTokenRepository.findAll().getFirst();
        storedToken.revoke();
        refreshTokenRepository.saveAndFlush(storedToken);

        String logoutRequestBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutRequestBody))
                .andExpect(status().isNoContent());

        RefreshToken tokenAfterSecondLogout = refreshTokenRepository.findById(storedToken.getId()).orElseThrow();
        assertThat(tokenAfterSecondLogout.getRevokedAt()).isNotNull();
    }
}
