package com.roleradar.auth.controller;

import com.roleradar.auth.dto.*;
import com.roleradar.auth.service.AuthService;
import com.roleradar.auth.dto.AuthTokens;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
    }

    @PostMapping("/login")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);

        return new AuthTokensResponse(
                "Bearer",
                tokens.accessToken(),
                tokens.accessTokenExpiresInSeconds(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresInSeconds()
        );
    }

    @PostMapping("/refresh")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokens tokens = authService.refresh(request.refreshToken());

        return new AuthTokensResponse(
                "Bearer",
                tokens.accessToken(),
                tokens.accessTokenExpiresInSeconds(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresInSeconds()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.getCurrentUser(jwt);
    }
}