package com.roleradar.auth.service;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.dto.RegisterRequest;
import com.roleradar.auth.dto.UserResponse;
import com.roleradar.auth.event.AuthEventPublisher;
import com.roleradar.auth.event.EmailVerificationRequestedEvent;
import com.roleradar.auth.exception.BadRequestException;
import com.roleradar.auth.exception.ConflictException;
import com.roleradar.auth.mapper.AuthMapper;
import com.roleradar.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final AuthEventPublisher authEventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthMapper authMapper,
                       EmailVerificationTokenService emailVerificationTokenService, AuthEventPublisher authEventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.authEventPublisher = authEventPublisher;
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
}