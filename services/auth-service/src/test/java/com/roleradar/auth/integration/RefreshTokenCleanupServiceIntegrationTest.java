package com.roleradar.auth.integration;

import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.repository.RefreshTokenRepository;
import com.roleradar.auth.repository.UserRepository;
import com.roleradar.auth.service.RefreshTokenCleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCleanupServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RefreshTokenCleanupService refreshTokenCleanupService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deleteExpiredAndOldRevokedTokens_shouldDeleteOnlyTokensPastRetentionCutoffs() {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.saveAndFlush(user);

        RefreshToken expiredTokenToDelete = new RefreshToken(
                user,
                "expired-token-to-delete",
                LocalDateTime.now().minusDays(2)
        );

        RefreshToken validTokenToKeep = new RefreshToken(
                user,
                "valid-token-to-keep",
                LocalDateTime.now().plusDays(2)
        );

        RefreshToken oldRevokedTokenToDelete = new RefreshToken(
                user,
                "old-revoked-token-to-delete",
                LocalDateTime.now().plusDays(2)
        );
        ReflectionTestUtils.setField(
                oldRevokedTokenToDelete,
                "revokedAt",
                LocalDateTime.now().minusDays(10)
        );

        RefreshToken recentlyRevokedTokenToKeep = new RefreshToken(
                user,
                "recently-revoked-token-to-keep",
                LocalDateTime.now().plusDays(2)
        );
        ReflectionTestUtils.setField(
                recentlyRevokedTokenToKeep,
                "revokedAt",
                LocalDateTime.now().minusDays(1)
        );

        refreshTokenRepository.saveAllAndFlush(List.of(
                expiredTokenToDelete,
                validTokenToKeep,
                oldRevokedTokenToDelete,
                recentlyRevokedTokenToKeep
        ));

        refreshTokenCleanupService.deleteExpiredAndOldRevokedTokens();

        List<String> remainingTokenHashes = refreshTokenRepository.findAll()
                .stream()
                .map(RefreshToken::getTokenHash)
                .toList();

        assertThat(remainingTokenHashes)
                .containsExactlyInAnyOrder(
                        "valid-token-to-keep",
                        "recently-revoked-token-to-keep"
                );
    }

    @Test
    void deleteExpiredAndOldRevokedTokens_whenNothingMatches_shouldLeaveTokensUntouched() {
        User user = new User("artem", "artem@example.com", passwordEncoder.encode("Password123"));
        user.verifyEmail(LocalDateTime.now());
        userRepository.saveAndFlush(user);

        RefreshToken validToken = new RefreshToken(
                user,
                "valid-token",
                LocalDateTime.now().plusDays(2)
        );

        RefreshToken recentlyRevokedToken = new RefreshToken(
                user,
                "recently-revoked-token",
                LocalDateTime.now().plusDays(2)
        );
        ReflectionTestUtils.setField(
                recentlyRevokedToken,
                "revokedAt",
                LocalDateTime.now().minusDays(1)
        );

        refreshTokenRepository.saveAllAndFlush(List.of(validToken, recentlyRevokedToken));

        refreshTokenCleanupService.deleteExpiredAndOldRevokedTokens();

        List<String> remainingTokenHashes = refreshTokenRepository.findAll()
                .stream()
                .map(RefreshToken::getTokenHash)
                .toList();

        assertThat(remainingTokenHashes)
                .containsExactlyInAnyOrder("valid-token", "recently-revoked-token");
    }
}
