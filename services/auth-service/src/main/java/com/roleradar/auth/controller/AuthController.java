package com.roleradar.auth.controller;

import com.roleradar.auth.dto.AuthTokensResponse;
import com.roleradar.auth.dto.LoginRequest;
import com.roleradar.auth.dto.RegisterRequest;
import com.roleradar.auth.dto.UserResponse;
import com.roleradar.auth.exception.UnauthorizedException;
import com.roleradar.auth.security.AccessTokenCookieFactory;
import com.roleradar.auth.security.RefreshTokenCookieFactory;
import com.roleradar.auth.service.AuthService;
import com.roleradar.auth.service.AuthTokens;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final AccessTokenCookieFactory accessTokenCookieFactory;

    public AuthController(AuthService authService,
                          RefreshTokenCookieFactory refreshTokenCookieFactory,
                          AccessTokenCookieFactory accessTokenCookieFactory) {
        this.authService = authService;
        this.refreshTokenCookieFactory = refreshTokenCookieFactory;
        this.accessTokenCookieFactory = accessTokenCookieFactory;
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
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest request,
                                    HttpServletResponse response) {
        AuthTokens tokens = authService.login(request);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessTokenCookieFactory
                        .createAccessTokenCookie(tokens.accessToken(), tokens.accessTokenExpiresInSeconds())
                        .toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory
                        .createRefreshTokenCookie(tokens.refreshToken(), authService.getRefreshTokenTtlSeconds())
                        .toString()
        );

        return new AuthTokensResponse("Bearer", tokens.accessTokenExpiresInSeconds());
    }

    @PostMapping("/refresh")
    public AuthTokensResponse refresh(HttpServletRequest request,
                                      HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        AuthTokens tokens = authService.refresh(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessTokenCookieFactory
                        .createAccessTokenCookie(tokens.accessToken(), tokens.accessTokenExpiresInSeconds())
                        .toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory
                        .createRefreshTokenCookie(tokens.refreshToken(), authService.getRefreshTokenTtlSeconds())
                        .toString()
        );

        return new AuthTokensResponse("Bearer", tokens.accessTokenExpiresInSeconds());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        authService.logout(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE,
                accessTokenCookieFactory.createDeleteAccessTokenCookie().toString());

        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory.createDeleteRefreshTokenCookie().toString());
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new UnauthorizedException("Refresh token cookie is missing.");
        }

        for (Cookie cookie : cookies) {
            if (RefreshTokenCookieFactory.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new UnauthorizedException("Refresh token cookie is missing.");
    }
}