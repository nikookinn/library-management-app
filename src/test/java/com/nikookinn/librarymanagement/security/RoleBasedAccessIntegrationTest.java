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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Role-Based Access Control Integration Tests")
class RoleBasedAccessIntegrationTest extends AbstractIntegrationTest {

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

        createUser("user@example.com", Role.USER);
        createUser("admin@example.com", Role.ADMIN);

        userToken = jwtService.createToken("user@example.com", Role.USER.name());
        adminToken = jwtService.createToken("admin@example.com", Role.ADMIN.name());
    }

    @Nested
    @DisplayName("USER Role Permissions")
    class UserRolePermissions {

        @Test
        @DisplayName("should allow user to read book catalog")
        void shouldAllowUserToReadBookCatalog() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should deny user from creating books")
        void shouldDenyUserFromCreatingBooks() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Test\",\"isbn\":\"123\",\"publishYear\":2024,\"categoryId\":1,\"totalCopies\":5}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @DisplayName("should deny user access to member management")
        void shouldDenyUserAccessToMemberManagement() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/members")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @DisplayName("should deny user access to loan management")
        void shouldDenyUserAccessToLoanManagement() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/loans")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }
    }

    @Nested
    @DisplayName("ADMIN Role Permissions")
    class AdminRolePermissions {

        @Test
        @DisplayName("should allow admin to read book catalog")
        void shouldAllowAdminToReadBookCatalog() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow admin to access member management")
        void shouldAllowAdminToAccessMemberManagement() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/members")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow admin to access loan management")
        void shouldAllowAdminToAccessLoanManagement() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/loans")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    private void createUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(role);
        userRepository.save(user);
    }
}

