package com.roleradar.notification.integration;

import com.roleradar.notification.event.EmailVerificationRequestedEvent;
import com.roleradar.notification.service.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmailNotificationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmailNotificationService emailNotificationService;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        reset(mailSender);
    }

    @Test
    void sendEmailVerificationEmail_shouldBuildAndSendExpectedMessage() {
        EmailVerificationRequestedEvent event = new EmailVerificationRequestedEvent(
                UUID.randomUUID(),
                "artem@example.com",
                "artem",
                "verification-token-123",
                LocalDateTime.of(2026, 3, 25, 18, 30)
        );

        emailNotificationService.sendEmailVerificationEmail(event);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly("artem@example.com");
        assertThat(message.getSubject()).isEqualTo("Verify your RoleRadar account");
        assertThat(message.getText()).contains("Hello artem,");
        assertThat(message.getText()).contains(
                "http://localhost:8080/api/v1/auth/verify-email?token=verification-token-123"
        );
        assertThat(message.getText()).contains("2026-03-25T18:30");
        assertThat(message.getText()).contains("If you did not create this account, please ignore this email.");
    }
}
