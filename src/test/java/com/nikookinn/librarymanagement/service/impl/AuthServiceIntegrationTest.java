package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.RegisterRequest;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.repository.UserRepository;
import com.nikookinn.librarymanagement.service.AuthService;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Auth Service Integration Tests")
class AuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should rollback user creation when registration fails")
    void shouldRollbackUserCreationWhenRegistrationFails() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser@example.com", "password123");
        
        // Force an exception during userRepository.save()
        doThrow(new RuntimeException("Simulated error during user save"))
                .when(userRepository).save(any(User.class));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated error during user save");

        // Verify that the user was not created
        assertThat(userRepository.findByEmail("newuser@example.com")).isEmpty();
    }
}
