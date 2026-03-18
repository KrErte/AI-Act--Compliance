package com.aiaudit.platform.auth;

import com.aiaudit.platform.auth.dto.*;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.UnauthorizedException;
import com.aiaudit.platform.organization.Organization;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private AuthResponse mockAuthResponse;
    private AppUser mockUser;
    private Organization mockOrg;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockOrg = anOrganization().build();
        mockUser = anAppUser().organization(mockOrg).build();
        mockAuthResponse = AuthResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .user(UserDto.from(mockUser))
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/register
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("should return 201 with AuthResponse for valid request")
        void register_validRequest_returns201() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful"))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                    .andExpect(jsonPath("$.data.user.email").value(mockUser.getEmail()));

            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return 400 when email is invalid format")
        void register_invalidEmail_returns400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("not-an-email");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("should return 400 when email already exists")
        void register_duplicateEmail_returns400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BadRequestException("Email already registered"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email already registered"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/login
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("should return 200 with AuthResponse for valid credentials")
        void login_validCredentials_returns200() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@test.com");
            request.setPassword("correctPassword");

            when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                    .andExpect(jsonPath("$.data.user").exists());

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("should return 401 for invalid credentials")
        void login_invalidCredentials_returns401() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@test.com");
            request.setPassword("wrongPassword");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid email or password"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void login_missingFields_returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            // email and password are both null/blank

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));

            verify(authService, never()).login(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/refresh
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        @Test
        @DisplayName("should return 200 with AuthResponse for valid refresh token")
        void refresh_validToken_returns200() throws Exception {
            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("valid-refresh-token");

            when(authService.refresh(any(RefreshRequest.class))).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"));

            verify(authService).refresh(any(RefreshRequest.class));
        }

        @Test
        @DisplayName("should return 401 for invalid refresh token")
        void refresh_invalidToken_returns401() throws Exception {
            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("invalid-token");

            when(authService.refresh(any(RefreshRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid refresh token"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/forgot-password
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("should return 200 for valid email")
        void forgotPassword_validEmail_returns200() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("user@test.com");

            doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"));

            verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/reset-password
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("should return 200 for valid reset request")
        void resetPassword_validRequest_returns200() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("valid-reset-token");
            request.setPassword("NewSecurePass123");

            doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password reset successfully"));

            verify(authService).resetPassword(any(ResetPasswordRequest.class));
        }

        @Test
        @DisplayName("should return 400 for expired token")
        void resetPassword_expiredToken_returns400() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("expired-token");
            request.setPassword("NewSecurePass123");

            doThrow(new BadRequestException("Reset token has expired"))
                    .when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Reset token has expired"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /auth/change-password
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/change-password")
    class ChangePassword {

        @Test
        @DisplayName("should return 200 for valid change-password request")
        void changePassword_validRequest_returns200() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldPassword");
            request.setNewPassword("NewSecurePass123");

            doNothing().when(authService).changePassword(any(), any(ChangePasswordRequest.class));

            mockMvc.perform(post("/auth/change-password")
                            .with(user(mockUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password changed successfully"));

            verify(authService).changePassword(any(), any(ChangePasswordRequest.class));
        }
    }
}
