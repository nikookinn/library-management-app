package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoginRequest;
import com.nikookinn.librarymanagement.dto.request.RegisterRequest;
import com.nikookinn.librarymanagement.dto.response.AuthResponse;
import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.exception.DuplicateResourceException;
import com.nikookinn.librarymanagement.repository.UserRepository;
import com.nikookinn.librarymanagement.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password123");
        loginRequest = new LoginRequest("test@example.com", "password123");
        
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.createToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUser.getEmail());
        assertThat(response.token()).isEqualTo("mock-jwt-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when email already exists during registration")
    void shouldThrowExceptionWhenEmailExistsDuringRegistration() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should login successfully")
    void shouldLoginSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.password(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.createToken(testUser.getEmail(), testUser.getRole().name())).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUser.getEmail());
        assertThat(response.token()).isEqualTo("mock-jwt-token");
    }

    @Test
    @DisplayName("should throw BadCredentialsException when email not found during login")
    void shouldThrowExceptionWhenEmailNotFoundDuringLogin() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Email or password is incorrect");
    }

    @Test
    @DisplayName("should throw BadCredentialsException when password does not match during login")
    void shouldThrowExceptionWhenPasswordDoesNotMatchDuringLogin() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.password(), testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Email or password is incorrect");
    }
}
