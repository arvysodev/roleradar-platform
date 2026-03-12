package com.roleradar.auth.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailVerificationRequestedEvent(
        UUID userId,
        String email,
        String username,
        String verificationToken,
        LocalDateTime expiresAt
) {
}