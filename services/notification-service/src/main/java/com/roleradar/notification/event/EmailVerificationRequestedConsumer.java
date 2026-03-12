package com.roleradar.notification.event;

import com.roleradar.notification.service.EmailNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationRequestedConsumer {

    private final EmailNotificationService emailNotificationService;

    public EmailVerificationRequestedConsumer(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @KafkaListener(
            topics = "${roleradar.kafka.topics.email-verification-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "emailVerificationKafkaListenerContainerFactory"
    )
    public void handle(EmailVerificationRequestedEvent event) {
        emailNotificationService.sendEmailVerificationEmail(event);
    }
}
