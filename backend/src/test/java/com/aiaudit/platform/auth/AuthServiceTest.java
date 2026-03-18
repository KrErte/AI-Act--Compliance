package com.aiaudit.platform.auth;

import com.aiaudit.platform.auth.dto.*;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.UnauthorizedException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Organization testOrg;
    private AppUser testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testOrg = anOrganization().build();
        testUser = anAppUser().organization(testOrg).build();
        testRefreshToken = aRefreshToken().user(testUser).build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // register
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should create organization and user, return AuthResponse")
        void register_happyPath() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenReturn(testOrg);
            when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> {
                AppUser u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });
            when(jwtService.generateAccessToken(any(AppUser.class))).thenReturn("access-jwt");
            when(refreshTokenService.createRefreshToken(any(AppUser.class))).thenReturn(testRefreshToken);

            AuthResponse response = authService.register(request);

            assertThat(response.getAccessToken()).isEqualTo("access-jwt");
            assertThat(response.getRefreshToken()).isEqualTo(testRefreshToken.getToken());
            assertThat(response.getUser()).isNotNull();

            // Verify organization was saved
            ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
            verify(organizationRepository).save(orgCaptor.capture());
            assertThat(orgCaptor.getValue().getName()).isEqualTo("Acme Corp");

            // Verify user was saved with correct fields
            ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(userCaptor.capture());
            AppUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
            assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$encoded");
            assertThat(savedUser.getFirstName()).isEqualTo("Jane");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.OWNER);
            assertThat(savedUser.getOrganization()).isEqualTo(testOrg);
        }

        @Test
        @DisplayName("should lowercase the email before checking and saving")
        void register_lowercasesEmail() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("User@EXAMPLE.COM");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenReturn(testOrg);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> {
                AppUser u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });
            when(jwtService.generateAccessToken(any(AppUser.class))).thenReturn("jwt");
            when(refreshTokenService.createRefreshToken(any(AppUser.class))).thenReturn(testRefreshToken);

            authService.register(request);

            verify(userRepository).existsByEmail("user@example.com");

            ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should throw BadRequestException when email already exists")
        void register_emailAlreadyExists() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("SecurePass123");
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setOrganizationName("Acme Corp");

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already registered");

            verify(organizationRepository, never()).save(any());
            verify(userRepository, never()).save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // login
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return AuthResponse for valid credentials")
        void login_happyPath() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@test.com");
            request.setPassword("correctPassword");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("correctPassword", testUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(testUser)).thenReturn("access-jwt");
            when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);

            AuthResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("access-jwt");
            assertThat(response.getRefreshToken()).isEqualTo(testRefreshToken.getToken());
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("should lowercase the email before lookup")
        void login_lowercasesEmail() {
            LoginRequest request = new LoginRequest();
            request.setEmail("User@TEST.COM");
            request.setPassword("correctPassword");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("correctPassword", testUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(testUser)).thenReturn("jwt");
            when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);

            authService.login(request);

            verify(userRepository).findByEmail("user@test.com");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when email not found")
        void login_emailNotFound() {
            LoginRequest request = new LoginRequest();
            request.setEmail("unknown@test.com");
            request.setPassword("password");

            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when password is wrong")
        void login_wrongPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@test.com");
            request.setPassword("wrongPassword");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when account is disabled")
        void login_accountDisabled() {
            AppUser disabledUser = anAppUser().enabled(false).organization(testOrg).build();

            LoginRequest request = new LoginRequest();
            request.setEmail("user@test.com");
            request.setPassword("correctPassword");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(disabledUser));
            when(passwordEncoder.matches("correctPassword", disabledUser.getPasswordHash())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Account is disabled");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // refresh
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("should rotate token and return new AuthResponse")
        void refresh_happyPath() {
            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("old-refresh-token");

            RefreshToken newToken = aRefreshToken()
                    .token("new-refresh-token")
                    .user(testUser)
                    .build();

            when(refreshTokenService.rotateToken("old-refresh-token")).thenReturn(newToken);
            when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-jwt");

            AuthResponse response = authService.refresh(request);

            assertThat(response.getAccessToken()).isEqualTo("new-access-jwt");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("should propagate exception when rotateToken fails")
        void refresh_invalidToken() {
            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("invalid-token");

            when(refreshTokenService.rotateToken("invalid-token"))
                    .thenThrow(new UnauthorizedException("Invalid refresh token"));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // forgotPassword
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPassword {

        @Test
        @DisplayName("should set reset token and send email when user exists")
        void forgotPassword_userExists() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("user@test.com");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

            authService.forgotPassword(request);

            ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(captor.capture());
            AppUser saved = captor.getValue();
            assertThat(saved.getPasswordResetToken()).isNotNull();
            assertThat(saved.getPasswordResetExpires()).isNotNull();
            assertThat(saved.getPasswordResetExpires()).isAfter(Instant.now());

            verify(emailService).sendPasswordResetEmail(eq("user@test.com"), anyString());
        }

        @Test
        @DisplayName("should not throw or send email when user does not exist (prevents enumeration)")
        void forgotPassword_userNotFound_noError() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("unknown@test.com");

            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Should not throw
            authService.forgotPassword(request);

            verify(userRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("should lowercase the email before lookup")
        void forgotPassword_lowercasesEmail() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("User@TEST.COM");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            authService.forgotPassword(request);

            verify(userRepository).findByEmail("user@test.com");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // resetPassword
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("should update password, clear token, and revoke refresh tokens")
        void resetPassword_happyPath() {
            testUser.setPasswordResetToken("valid-token");
            testUser.setPasswordResetExpires(Instant.now().plus(30, ChronoUnit.MINUTES));

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("valid-token");
            request.setPassword("NewSecurePass123");

            when(userRepository.findByPasswordResetToken("valid-token")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("NewSecurePass123")).thenReturn("$2a$10$newEncoded");
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

            authService.resetPassword(request);

            ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(captor.capture());
            AppUser saved = captor.getValue();
            assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$newEncoded");
            assertThat(saved.getPasswordResetToken()).isNull();
            assertThat(saved.getPasswordResetExpires()).isNull();

            verify(refreshTokenService).revokeAllUserTokens(testUser.getId());
        }

        @Test
        @DisplayName("should throw BadRequestException when token is invalid")
        void resetPassword_invalidToken() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("bad-token");
            request.setPassword("NewSecurePass123");

            when(userRepository.findByPasswordResetToken("bad-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid or expired reset token");

            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllUserTokens(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when token has expired")
        void resetPassword_expiredToken() {
            testUser.setPasswordResetToken("expired-token");
            testUser.setPasswordResetExpires(Instant.now().minus(1, ChronoUnit.HOURS));

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("expired-token");
            request.setPassword("NewSecurePass123");

            when(userRepository.findByPasswordResetToken("expired-token")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Reset token has expired");

            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllUserTokens(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when passwordResetExpires is null")
        void resetPassword_nullExpiry() {
            testUser.setPasswordResetToken("token-no-expiry");
            testUser.setPasswordResetExpires(null);

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("token-no-expiry");
            request.setPassword("NewSecurePass123");

            when(userRepository.findByPasswordResetToken("token-no-expiry")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Reset token has expired");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // changePassword
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("should update password and revoke all refresh tokens")
        void changePassword_happyPath() {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldPassword");
            request.setNewPassword("NewSecurePass123");

            when(passwordEncoder.matches("oldPassword", testUser.getPasswordHash())).thenReturn(true);
            when(passwordEncoder.encode("NewSecurePass123")).thenReturn("$2a$10$newHash");
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

            authService.changePassword(testUser, request);

            ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$newHash");

            verify(refreshTokenService).revokeAllUserTokens(testUser.getId());
        }

        @Test
        @DisplayName("should throw BadRequestException when current password is wrong")
        void changePassword_wrongCurrentPassword() {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("wrongPassword");
            request.setNewPassword("NewSecurePass123");

            when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(testUser, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Current password is incorrect");

            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllUserTokens(any());
        }
    }
}
