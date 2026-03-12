package com.roleradar.auth.service;

import com.roleradar.auth.domain.RefreshToken;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.domain.UserStatus;
import com.roleradar.auth.dto.AuthTokensResponse;
import com.roleradar.auth.dto.LoginRequest;
import com.roleradar.auth.dto.RegisterRequest;
import com.roleradar.auth.dto.UserResponse;
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
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final AuthEventPublisher authEventPublisher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthMapper authMapper,
                       EmailVerificationTokenService emailVerificationTokenService, AuthEventPublisher authEventPublisher, RefreshTokenRepository refreshTokenRepository, JwtService jwtService, TokenHasher tokenHasher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.authEventPublisher = authEventPublisher;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.tokenHasher = tokenHasher;
    }

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

        user.startEmailVerification(tokenHash, LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        LocalDateTime expiresAt = user.getEmailVerificationTokenExpiresAt();

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

    public AuthTokensResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            throw new UnauthorizedException("Email is not verified. Check your mailbox.");
        }

        return issueTokens(user);
    }

    private AuthTokensResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);

        String rawRefreshToken = generateRefreshToken();
        String refreshTokenHash = tokenHasher.sha256Base64Url(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenHash,
                LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds())
        );

        refreshTokenRepository.save(refreshToken);

        return new AuthTokensResponse(
                "Bearer",
                accessToken,
                jwtService.getAccessTokenTtlSeconds(),
                rawRefreshToken
        );
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}