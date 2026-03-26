package com.roleradar.auth.integration;

import com.jayway.jsonpath.JsonPath;
import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.repository.RefreshTokenRepository;
import com.roleradar.auth.repository.UserRepository;
import com.roleradar.auth.security.TokenHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRefreshTokenIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenHasher tokenHasher;

    @Test
    @Transactional
    void refresh_shouldRotateRefreshTokenAndReturnNewTokenPair() throws Exception {
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String initialRefreshToken = JsonPath.read(loginResponseBody, "$.refreshToken");

        List<RefreshToken> tokensAfterLogin = refreshTokenRepository.findAll();
        assertThat(tokensAfterLogin).hasSize(1);

        RefreshToken originalStoredToken = tokensAfterLogin.getFirst();
        assertThat(originalStoredToken.getRevokedAt()).isNull();
        assertThat(originalStoredToken.getExpiresAt()).isNotNull();
        assertThat(originalStoredToken.getUser().getEmail()).isEqualTo("artem@example.com");

        String refreshRequestBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(initialRefreshToken);

        String refreshResponseBody = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresInSeconds").isNumber())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshTokenExpiresInSeconds").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String rotatedRefreshToken = JsonPath.read(refreshResponseBody, "$.refreshToken");

        assertThat(rotatedRefreshToken).isNotEqualTo(initialRefreshToken);

        List<RefreshToken> allTokens = refreshTokenRepository.findAll();
        assertThat(allTokens).hasSize(2);

        RefreshToken revokedToken = allTokens.stream()
                .filter(token -> token.getId().equals(originalStoredToken.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(revokedToken.getRevokedAt()).isNotNull();

        List<RefreshToken> activeTokens = allTokens.stream()
                .filter(token -> token.getRevokedAt() == null)
                .toList();

        assertThat(activeTokens).hasSize(1);

        RefreshToken activeToken = activeTokens.getFirst();
        assertThat(activeToken.getUser().getEmail()).isEqualTo("artem@example.com");
        assertThat(activeToken.getExpiresAt()).isAfter(LocalDateTime.now());

        RefreshToken newestToken = allTokens.stream()
                .max(Comparator.comparing(RefreshToken::getCreatedAt))
                .orElseThrow();

        assertThat(newestToken.getRevokedAt()).isNull();
        assertThat(newestToken.getId()).isEqualTo(activeToken.getId());
    }

    @Test
    void refresh_whenTokenIsRevoked_shouldReturnUnauthorized() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.save(user);

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
        refreshTokenRepository.save(storedToken);

        String refreshRequestBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Refresh token is no longer valid."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/refresh"));
    }

    @Test
    void refresh_whenTokenIsExpired_shouldReturnUnauthorized() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        user = userRepository.save(user);

        String rawRefreshToken = UUID.randomUUID().toString();
        String refreshTokenHash = tokenHasher.sha256Base64Url(rawRefreshToken);

        RefreshToken expiredRefreshToken = new RefreshToken(
                user,
                refreshTokenHash,
                LocalDateTime.now().minusMinutes(1)
        );
        refreshTokenRepository.save(expiredRefreshToken);

        String refreshRequestBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(rawRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Refresh token is no longer valid."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/refresh"));
    }

    @Test
    void refresh_whenTokenIsInvalid_shouldReturnUnauthorized() throws Exception {
        String requestBody = """
                {
                  "refreshToken": "invalid-refresh-token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid refresh token."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/refresh"));
    }

    @Test
    void refresh_whenUserIsNotActive_shouldReturnUnauthorized() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        user = userRepository.save(user);

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

        user.disable();
        userRepository.saveAndFlush(user);

        String refreshRequestBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("User is not active."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/refresh"));
    }
}
