package com.nikookinn.librarymanagement.actuator;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Actuator Endpoint Exposure and Security Integration Tests")
class ActuatorSecurityIntegrationTest extends AbstractIntegrationTest {

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

        User user = new User();
        user.setEmail("actuator-user@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        User admin = new User();
        admin.setEmail("actuator-admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        userToken = jwtService.createToken("actuator-user@example.com", Role.USER.name());
        adminToken = jwtService.createToken("actuator-admin@example.com", Role.ADMIN.name());
    }

    @Nested
    @DisplayName("Health Endpoint")
    class HealthEndpoint {

        @Test
        @DisplayName("should be publicly accessible without exposing component details")
        void shouldBePubliclyAccessibleWithoutComponentDetails() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.components").doesNotExist());
        }

        @Test
        @DisplayName("should expose component details to an authenticated admin")
        void shouldExposeComponentDetailsToAuthenticatedAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/actuator/health")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.components.db.status").value("UP"));
        }
    }

    @Nested
    @DisplayName("Prometheus Endpoint")
    class PrometheusEndpoint {

        @Test
        @DisplayName("should return 401 when no credentials are provided")
        void shouldReturn401WhenNoCredentialsAreProvided() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/actuator/prometheus"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when a regular user requests metrics")
        void shouldReturn403WhenRegularUserRequestsMetrics() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/actuator/prometheus")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should expose expected JVM, HTTP and datasource metrics to an admin")
        void shouldExposeExpectedMetricsToAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/actuator/prometheus")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_memory_used_bytes")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_classes_loaded_classes")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_threads_live_threads")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("hikaricp_connections")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("process_uptime_seconds")));
        }
    }

    @Nested
    @DisplayName("Other Actuator Endpoints")
    class OtherActuatorEndpoints {

        @Test
        @DisplayName("should not expose env variable values even to an admin")
        void shouldNotExposeEnvEndpointEvenToAdmin() throws Exception {
            // Act & Assert - endpoint is not in management.endpoints.web.exposure.include,
            // so no handler is mapped and no environment data is ever returned.
            mockMvc.perform(get("/actuator/env")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus()).isNotEqualTo(200))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("propertySources"))));
        }

        @Test
        @DisplayName("should not expose bean definitions even to an admin")
        void shouldNotExposeBeansEndpointEvenToAdmin() throws Exception {
            // Act & Assert - endpoint is not in management.endpoints.web.exposure.include,
            // so no handler is mapped and no bean metadata is ever returned.
            mockMvc.perform(get("/actuator/beans")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus()).isNotEqualTo(200))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("\"beans\""))));
        }
    }
}
