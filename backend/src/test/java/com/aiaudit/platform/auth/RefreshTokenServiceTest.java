package com.aiaudit.platform.auth;

import com.aiaudit.platform.common.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createRefreshToken_success() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiry", 604800000L);
        AppUser user = anAppUser().build();
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertFalse(result.isRevoked());
        assertTrue(result.getExpiresAt().isAfter(Instant.now()));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void rotateToken_validToken_returnsNewToken() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiry", 604800000L);
        AppUser user = anAppUser().build();
        RefreshToken existing = aRefreshToken()
                .user(user)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(existing.getToken())).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.rotateToken(existing.getToken());

        assertNotNull(result);
        assertTrue(existing.isRevoked());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void rotateToken_invalidToken_throwsUnauthorized() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken("bad-token"));
    }

    @Test
    void rotateToken_revokedToken_revokesAllAndThrows() {
        AppUser user = anAppUser().build();
        RefreshToken existing = aRefreshToken()
                .user(user)
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(existing.getToken())).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken(existing.getToken()));
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    void rotateToken_expiredToken_throwsUnauthorized() {
        AppUser user = anAppUser().build();
        RefreshToken existing = aRefreshToken()
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(refreshTokenRepository.findByToken(existing.getToken())).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken(existing.getToken()));
    }

    @Test
    void revokeAllUserTokens_callsRepository() {
        UUID userId = UUID.randomUUID();
        refreshTokenService.revokeAllUserTokens(userId);
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    void rotateToken_revokedToken_containsMessageAboutReuse() {
        AppUser user = anAppUser().build();
        RefreshToken existing = aRefreshToken()
                .user(user)
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(existing.getToken())).thenReturn(Optional.of(existing));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken(existing.getToken()));
        assertTrue(ex.getMessage().contains("reuse"));
    }

    @Test
    void rotateToken_expiredToken_containsExpiredMessage() {
        AppUser user = anAppUser().build();
        RefreshToken existing = aRefreshToken()
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(refreshTokenRepository.findByToken(existing.getToken())).thenReturn(Optional.of(existing));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken(existing.getToken()));
        assertTrue(ex.getMessage().contains("expired"));
    }
}
