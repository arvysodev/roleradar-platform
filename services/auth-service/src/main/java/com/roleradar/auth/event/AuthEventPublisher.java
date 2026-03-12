package com.roleradar.auth.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String emailVerificationRequestedTopic;

    public AuthEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${roleradar.kafka.topics.email-verification-requested}") String emailVerificationRequestedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailVerificationRequestedTopic = emailVerificationRequestedTopic;
    }

    public void publishEmailVerificationRequested(EmailVerificationRequestedEvent event) {
        kafkaTemplate.send(emailVerificationRequestedTopic, event.userId().toString(), event);
    }
}