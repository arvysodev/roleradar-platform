package com.roleradar.auth.integration;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.event.AuthEventPublisher;
import com.roleradar.auth.event.EmailVerificationRequestedEvent;
import com.roleradar.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRegistrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthEventPublisher authEventPublisher;

    @Test
    void register_shouldCreatePendingUserAndPublishVerificationEvent() throws Exception {
        String requestBody = """
                {
                  "email": "ARTEM@example.com",
                  "username": "ArTy_Dev",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("artem@example.com"))
                .andExpect(jsonPath("$.username").value("arty_dev"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        User savedUser = userRepository.findByEmail("artem@example.com").orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo("arty_dev");
        assertThat(savedUser.getRole().name()).isEqualTo("USER");
        assertThat(savedUser.getStatus().name()).isEqualTo("PENDING");
        assertThat(savedUser.isEmailVerified()).isFalse();

        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123");
        assertThat(passwordEncoder.matches("Password123", savedUser.getPasswordHash())).isTrue();

        assertThat(savedUser.getEmailVerificationTokenHash()).isNotBlank();
        assertThat(savedUser.getEmailVerificationTokenExpiresAt()).isNotNull();

        verify(authEventPublisher, times(1))
                .publishEmailVerificationRequested(any(EmailVerificationRequestedEvent.class));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturnConflict() throws Exception {
        userRepository.save(
                new User(
                        "existing-user",
                        "taken@example.com",
                        passwordEncoder.encode("Password123")
                )
        );

        String requestBody = """
                {
                  "email": "taken@example.com",
                  "username": "new-user",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Email is already in use."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/conflict"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/register"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void register_whenUsernameAlreadyExists_shouldReturnConflict() throws Exception {
        userRepository.save(
                new User(
                        "taken-username",
                        "existing@example.com",
                        passwordEncoder.encode("Password123")
                )
        );

        String requestBody = """
            {
              "email": "new@example.com",
              "username": "taken-username",
              "password": "Password123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Username is already in use."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/conflict"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/register"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void register_whenPayloadIsInvalid_shouldReturnBadRequest() throws Exception {
        String requestBody = """
            {
              "email": "not-an-email",
              "username": "ab",
              "password": "123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Validation failed."))
                .andExpect(jsonPath("$.type").value("https://roleradar.app/problems/bad-request"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auth/register"));

        assertThat(userRepository.count()).isZero();
    }
}
