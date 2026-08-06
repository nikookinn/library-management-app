package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoginRequest;
import com.nikookinn.librarymanagement.dto.request.RegisterRequest;
import com.nikookinn.librarymanagement.dto.response.AuthResponse;
import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.exception.DuplicateResourceException;
import com.nikookinn.librarymanagement.repository.UserRepository;
import com.nikookinn.librarymanagement.service.AuthService;
import com.nikookinn.librarymanagement.service.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email or password is incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email or password is incorrect");
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String token = jwtService.createToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getRole().name());
    }
}
