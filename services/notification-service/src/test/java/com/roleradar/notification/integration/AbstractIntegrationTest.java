package com.roleradar.notification.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "roleradar.auth.verify-email-base-url",
                () -> "http://localhost:8080/api/v1/auth/verify-email"
        );
        registry.add(
                "roleradar.kafka.topics.email-verification-requested",
                () -> "email-verification-requested"
        );
        registry.add(
                "spring.kafka.consumer.group-id",
                () -> "notification-service-test"
        );
        registry.add(
                "management.health.mail.enabled",
                () -> "false"
        );
    }
}
