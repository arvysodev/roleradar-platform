package com.roleradar.auth.service;

import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.domain.UserStatus;
import com.roleradar.auth.dto.*;
import com.roleradar.auth.event.AuthEventPublisher;
import com.roleradar.auth.event.EmailVerificationRequestedEvent;
import com.roleradar.auth.exception.BadRequestException;
import com.roleradar.auth.exception.ConflictException;
import com.roleradar.auth.exception.UnauthorizedException;
import com.roleradar.auth.mapper.AuthMapper;
import com.roleradar.auth.repository.RefreshTokenRepository;
import com.roleradar.auth.repository.UserRepository;
import com.roleradar.auth.security.JwtService;
import com.roleradar.auth.security.TokenHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final AuthEventPublisher authEventPublisher;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthMapper authMapper,
                       EmailVerificationTokenService emailVerificationTokenService,
                       AuthEventPublisher authEventPublisher,
                       JwtService jwtService,
                       TokenHasher tokenHasher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.authEventPublisher = authEventPublisher;
        this.jwtService = jwtService;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedUsername = request.username().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already in use.");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username is already in use.");
        }

        User user = new User(
                normalizedUsername,
                normalizedEmail,
                passwordEncoder.encode(request.password())
        );

        String rawToken = emailVerificationTokenService.generateRawToken();
        String tokenHash = emailVerificationTokenService.hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        user.startEmailVerification(tokenHash, expiresAt);

        User savedUser = userRepository.save(user);

        authEventPublisher.publishEmailVerificationRequested(
                new EmailVerificationRequestedEvent(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getUsername(),
                        rawToken,
                        expiresAt
                )
        );

        return authMapper.toUserResponse(savedUser);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        String tokenHash = emailVerificationTokenService.hashToken(rawToken);

        User user = userRepository.findByEmailVerificationTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid email verification token."));

        if (user.getEmailVerificationTokenExpiresAt() == null ||
                user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Email verification token has expired.");
        }

        user.verifyEmail(LocalDateTime.now());
        userRepository.save(user);
    }

    public AuthTokens login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            throw new UnauthorizedException("Email is not verified.");
        }

        return issueTokens(user);
    }

    public AuthTokens refresh(String rawRefreshToken) {
        String refreshTokenHash = tokenHasher.sha256Base64Url(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new UnauthorizedException("Refresh token is no longer valid.");
        }

        User user = storedToken.getUser();

        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            throw new UnauthorizedException("User is not active.");
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        return issueTokens(user);
    }

    public void logout(String rawRefreshToken) {
        String refreshTokenHash = tokenHasher.sha256Base64Url(rawRefreshToken);

        refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .ifPresent(token -> {
                    if (!token.isRevoked()) {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    }
                });
    }

    public MeResponse getCurrentUser(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");

        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("preferred_username"),
                roles == null ? List.of() : roles
        );
    }

    public long getRefreshTokenTtlSeconds() {
        return jwtService.getRefreshTokenTtlSeconds();
    }

    private AuthTokens issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);

        String rawRefreshToken = generateRefreshToken();
        String refreshTokenHash = tokenHasher.sha256Base64Url(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenHash,
                LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds())
        );

        refreshTokenRepository.save(refreshToken);

        return new AuthTokens(
                accessToken,
                jwtService.getAccessTokenTtlSeconds(),
                rawRefreshToken,
                jwtService.getRefreshTokenTtlSeconds()
        );
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}