package com.nikookinn.librarymanagement.config;

import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.repository.UserRepository;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Default Admin Initializer Integration Tests")
class DefaultAdminInitializerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommandLineRunner createDefaultAdmin;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        createDefaultAdmin.run();
    }

    @Nested
    @DisplayName("Admin Account Creation")
    class AdminAccountCreation {

        @Test
        @DisplayName("should create one default admin account when application starts")
        void shouldCreateDefaultAdminOnStartup() {
            // Arrange
            List<User> users = userRepository.findAll();

            // Act & Assert
            long adminCount = users.stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .count();

            assertThat(adminCount).isEqualTo(1);
        }

        @Test
        @DisplayName("should create admin with email 'admin@test.local'")
        void shouldCreateAdminWithCorrectEmail() {
            // Act & Assert
            assertThat(userRepository.findByEmail("admin@test.local"))
                    .isPresent()
                    .get()
                    .satisfies(user -> {
                        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
                        assertThat(user.getEmail()).isEqualTo("admin@test.local");
                        assertThat(user.getPasswordHash()).isNotBlank();
                    });
        }

        @Test
        @DisplayName("should create admin with hashed password")
        void shouldCreateAdminWithHashedPassword() {
            // Act & Assert
            User admin = userRepository.findByEmail("admin@test.local").orElse(null);

            assertThat(admin).isNotNull();
            assertThat(admin.getPasswordHash()).isNotBlank();
            assertThat(admin.getPasswordHash()).isNotEqualTo("initial-password");
        }
    }

    @Nested
    @DisplayName("Initial Database State")
    class InitialDatabaseState {

        @BeforeEach
        void setUp() {
            // Clear database before each test (optional verification)
        }

        @Test
        @DisplayName("should have exactly one user after initialization")
        void shouldHaveExactlyOneUserAfterInitialization() {
            // Act & Assert
            long totalUsers = userRepository.count();
            assertThat(totalUsers).isEqualTo(1);
        }

        @Test
        @DisplayName("should have one admin and zero users")
        void shouldHaveOneAdminAndZeroUsers() {
            // Act
            List<User> allUsers = userRepository.findAll();
            long adminCount = allUsers.stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .count();
            long userCount = allUsers.stream()
                    .filter(user -> user.getRole() == Role.USER)
                    .count();

            // Assert
            assertThat(adminCount).isEqualTo(1);
            assertThat(userCount).isEqualTo(0);
        }
    }
}

