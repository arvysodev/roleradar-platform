package com.roleradar.auth.service;

import com.roleradar.auth.domain.Role;
import com.roleradar.auth.domain.User;
import com.roleradar.auth.dto.RegisterRequest;
import com.roleradar.auth.dto.UserResponse;
import com.roleradar.auth.exception.ConflictException;
import com.roleradar.auth.mapper.AuthMapper;
import com.roleradar.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
    }

    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already in use");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        return authMapper.toUserResponse(savedUser);
    }
}
