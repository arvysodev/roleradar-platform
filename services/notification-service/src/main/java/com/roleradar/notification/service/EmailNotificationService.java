package com.roleradar.notification.service;

import com.roleradar.notification.event.EmailVerificationRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String verifyEmailBaseUrl;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${roleradar.auth.verify-email-base-url}") String verifyEmailBaseUrl
    ) {
        this.mailSender = mailSender;
        this.verifyEmailBaseUrl = verifyEmailBaseUrl;
    }

    public void sendEmailVerificationEmail(EmailVerificationRequestedEvent event) {
        String verificationLink = verifyEmailBaseUrl + "?token=" + event.verificationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Verify your RoleRadar account");
        message.setText("""
                Hello %s,

                Please verify your email by clicking the link below:

                %s

                This link will expire at: %s

                If you did not create this account, please ignore this email.
                """.formatted(
                event.username(),
                verificationLink,
                event.expiresAt()
        ));

        mailSender.send(message);
    }
}