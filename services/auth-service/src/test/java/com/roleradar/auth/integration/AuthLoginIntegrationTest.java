package com.roleradar.auth.integration;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.repository.RefreshTokenRepository;
import com.roleradar.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void login_shouldReturnTokenPairAndPersistRefreshToken() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.save(user);

        String requestBody = """
                {
                  "email": "artem@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresInSeconds").isNumber())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshTokenExpiresInSeconds").isNumber());

        assertThat(refreshTokenRepository.count()).isEqualTo(1);

        var savedRefreshToken = refreshTokenRepository.findAll().getFirst();
        assertThat(savedRefreshToken.getUser().getEmail()).isEqualTo("artem@example.com");
        assertThat(savedRefreshToken.getTokenHash()).isNotBlank();
        assertThat(savedRefreshToken.getExpiresAt()).isNotNull();
        assertThat(savedRefreshToken.getRevokedAt()).isNull();
    }

    @Test
    void login_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.save(user);

        String requestBody = """
                {
                  "email": "artem@example.com",
                  "password": "WrongPassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid email or password."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/login"));

        assertThat(refreshTokenRepository.count()).isZero();
    }

    @Test
    void login_whenEmailIsNotVerified_shouldReturnUnauthorized() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        String requestBody = """
                {
                  "email": "artem@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Email is not verified."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/login"));

        assertThat(refreshTokenRepository.count()).isZero();
    }
}
