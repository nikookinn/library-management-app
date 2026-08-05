package com.nikookinn.librarymanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt-token-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("JWT Token Handling Integration Tests")
class JwtTokenIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${jwt.secret}")
    private String secret;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("Token Expiration Handling")
    class TokenExpiration {

        @Test
        @DisplayName("should return 401 when token has expired")
        void shouldReturn401WhenTokenHasExpired() throws Exception {
            // Arrange
            String expiredToken = Jwts.builder()
                    .subject("user@example.com")
                    .issuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                    .expiration(new Date(System.currentTimeMillis() - 3_600_000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                    .compact();

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Token has expired"));
        }
    }

    @Nested
    @DisplayName("Malformed Token Handling")
    class MalformedToken {

        @Test
        @DisplayName("should return 401 when token is malformed")
        void shouldReturn401WhenTokenIsMalformed() throws Exception {
            // Arrange
            String malformedToken = "invalid.token.format";

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + malformedToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Token is malformed"));
        }

        @Test
        @DisplayName("should return 401 when token is empty string")
        void shouldReturn401WhenTokenIsEmpty() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer "))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }
    }

    @Nested
    @DisplayName("Token Signature Validation")
    class TokenSignature {

        @Test
        @DisplayName("should return 401 when token signature is invalid")
        void shouldReturn401WhenSignatureIsInvalid() throws Exception {
            // Arrange - Create valid-looking token with wrong signature
            String validToken = Jwts.builder()
                    .subject("user@example.com")
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                    .compact();

            String tamperedToken = validToken.substring(0, validToken.length() - 10) + "corrupted!";

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + tamperedToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Invalid token"));
        }
    }
}
