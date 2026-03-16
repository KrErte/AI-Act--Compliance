package com.aiaudit.platform.auth;

import com.aiaudit.platform.organization.Organization;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        // 64+ character secret for HMAC-SHA512
        String secret = "test-secret-key-minimum-64-characters-long-for-hmac-sha512-signing-algorithm-test";
        jwtService = new JwtService(secret, 900000L); // 15 minutes

        Organization org = Organization.builder()
                .id(UUID.randomUUID())
                .name("Test Org")
                .build();

        testUser = AppUser.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.OWNER)
                .organization(org)
                .build();
    }

    @Test
    void generateAccessToken_returnsNonNullToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_extractsCorrectClaims() {
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getEmail(), claims.get("email", String.class));
        assertEquals(testUser.getRole().name(), claims.get("role", String.class));
        assertEquals(testUser.getOrganization().getId().toString(), claims.get("organizationId", String.class));
    }

    @Test
    void getUserId_returnsCorrectId() {
        String token = jwtService.generateAccessToken(testUser);
        UUID userId = jwtService.getUserId(token);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void getEmail_returnsCorrectEmail() {
        String token = jwtService.generateAccessToken(testUser);
        String email = jwtService.getEmail(token);
        assertEquals(testUser.getEmail(), email);
    }

    @Test
    void getOrganizationId_returnsCorrectOrgId() {
        String token = jwtService.generateAccessToken(testUser);
        UUID orgId = jwtService.getOrganizationId(token);
        assertEquals(testUser.getOrganization().getId(), orgId);
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtService.generateAccessToken(testUser);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtService.isTokenValid(tampered));
    }
}
