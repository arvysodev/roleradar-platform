package com.roleradar.auth.repository;

import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);

    long deleteByExpiresAtBefore(LocalDateTime cutoff);

    long deleteByRevokedAtBefore(LocalDateTime cutoff);
}