package com.nikookinn.librarymanagement.security;

import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.repository.UserRepository;
import com.nikookinn.librarymanagement.service.JwtService;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Authentication and Authorization Error Handling Integration Tests")
class AuthenticationErrorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Create regular user
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        // Create admin user
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        userToken = jwtService.createToken("user@example.com", Role.USER.name());
        adminToken = jwtService.createToken("admin@example.com", Role.ADMIN.name());
    }

    @Nested
    @DisplayName("Missing Token")
    class MissingToken {

        @Test
        @DisplayName("should return 401 when authorization header is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/books"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", "Bearer"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("should return 401 when authorization header has no Bearer prefix")
        void shouldReturn401WhenAuthHeaderHasNoBearerPrefix() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Basic some-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", "Bearer"))
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("Invalid Credentials")
    class InvalidCredentials {

        @Test
        @DisplayName("should return 401 when login credentials are invalid")
        void shouldReturn401WhenLoginCredentialsAreInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("should return 401 when password is incorrect")
        void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@example.com\",\"password\":\"wrongpassword\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }
    }

    @Nested
    @DisplayName("Role-Based Access Control")
    class RoleBasedAccessControl {

        @Test
        @DisplayName("should return 403 when user does not have required role for protected endpoint")
        void shouldReturn403WhenUserDoesNotHaveRequiredRole() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/members")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @DisplayName("should return 200 when admin accesses admin-only endpoint")
        void shouldReturn200WhenAdminAccessesAdminEndpoint() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/members")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 403 when user tries to create book")
        void shouldReturn403WhenUserTriesToCreateBook() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Test\",\"isbn\":\"123\",\"publishYear\":2024,\"categoryId\":1,\"totalCopies\":5}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }
    }

    @Nested
    @DisplayName("User Deletion Scenarios")
    class UserDeletionScenarios {

        @Test
        @DisplayName("should return 401 when user account is deleted but token is still valid")
        void shouldReturn401WhenUserIsDeletedButTokenIsValid() throws Exception {
            // Arrange - Generate valid token for user
            String validToken = jwtService.createToken("deleted@example.com", Role.USER.name());

            // Act & Assert - User doesn't exist in database
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("User not found"));
        }
    }
}

