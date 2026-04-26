package com.roleradar.auth.integration;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.repository.UserRepository;
import com.roleradar.auth.service.EmailVerificationTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthEmailVerificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationTokenService emailVerificationTokenService;

    @Test
    void verifyEmail_withValidToken_shouldActivateUser() throws Exception {
        String rawToken = emailVerificationTokenService.generateRawToken();
        String tokenHash = emailVerificationTokenService.hashToken(rawToken);

        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.startEmailVerification(tokenHash, LocalDateTime.now().plusHours(24));
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", rawToken))
                .andExpect(status().isFound());

        User verifiedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(verifiedUser.isEmailVerified()).isTrue();
        assertThat(verifiedUser.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(verifiedUser.getEmailVerifiedAt()).isNotNull();
        assertThat(verifiedUser.getEmailVerificationTokenHash()).isNull();
        assertThat(verifiedUser.getEmailVerificationTokenExpiresAt()).isNull();
    }

    @Test
    void verifyEmail_withExpiredToken_shouldReturnBadRequest() throws Exception {
        String rawToken = emailVerificationTokenService.generateRawToken();
        String tokenHash = emailVerificationTokenService.hashToken(rawToken);

        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.startEmailVerification(tokenHash, LocalDateTime.now().minusMinutes(1));
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", rawToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Email verification token has expired."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/bad-request"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/verify-email"));

        User unchangedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(unchangedUser.isEmailVerified()).isFalse();
        assertThat(unchangedUser.getStatus().name()).isEqualTo("PENDING");
        assertThat(unchangedUser.getEmailVerificationTokenHash()).isEqualTo(tokenHash);
        assertThat(unchangedUser.getEmailVerificationTokenExpiresAt()).isNotNull();
    }

    @Test
    void verifyEmail_withInvalidToken_shouldReturnBadRequest() throws Exception {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.startEmailVerification("some-valid-token-hash", LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", "invalid-raw-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid email verification token."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/bad-request"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/verify-email"));

        User unchangedUser = userRepository.findByEmail("artem@example.com").orElseThrow();

        assertThat(unchangedUser.isEmailVerified()).isFalse();
        assertThat(unchangedUser.getStatus().name()).isEqualTo("PENDING");
        assertThat(unchangedUser.getEmailVerificationTokenHash()).isEqualTo("some-valid-token-hash");
        assertThat(unchangedUser.getEmailVerificationTokenExpiresAt()).isNotNull();
    }
}
