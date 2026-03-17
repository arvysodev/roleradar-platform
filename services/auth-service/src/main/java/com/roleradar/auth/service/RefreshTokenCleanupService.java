package com.roleradar.auth.service;

import com.roleradar.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expiredRetentionDays;
    private final long revokedRetentionDays;

    public RefreshTokenCleanupService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${roleradar.cleanup.refresh-tokens.expired-retention-days}") long expiredRetentionDays,
            @Value("${roleradar.cleanup.refresh-tokens.revoked-retention-days}") long revokedRetentionDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expiredRetentionDays = expiredRetentionDays;
        this.revokedRetentionDays = revokedRetentionDays;
    }

    @Transactional
    @Scheduled(cron = "${roleradar.cleanup.refresh-tokens.cron}")
    public void deleteExpiredAndOldRevokedTokens() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expiredCutoff = now.minusDays(expiredRetentionDays);
        LocalDateTime revokedCutoff = now.minusDays(revokedRetentionDays);

        long expiredDeleted = refreshTokenRepository.deleteByExpiresAtBefore(expiredCutoff);
        long revokedDeleted = refreshTokenRepository.deleteByRevokedAtBefore(revokedCutoff);

        long totalDeleted = expiredDeleted + revokedDeleted;

        if (totalDeleted > 0) {
            log.info(
                    "Deleted refresh tokens: expiredCount={} revokedCount={} expiredCutoff={} revokedCutoff={}",
                    expiredDeleted,
                    revokedDeleted,
                    expiredCutoff,
                    revokedCutoff
            );
        }
    }
}
