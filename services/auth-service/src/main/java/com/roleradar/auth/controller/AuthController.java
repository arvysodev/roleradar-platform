package com.roleradar.auth.controller;

import com.roleradar.auth.dto.AuthTokensResponse;
import com.roleradar.auth.dto.LoginRequest;
import com.roleradar.auth.dto.RegisterRequest;
import com.roleradar.auth.dto.UserResponse;
import com.roleradar.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}