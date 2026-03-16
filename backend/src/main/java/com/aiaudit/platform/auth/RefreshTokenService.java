package com.aiaudit.platform.auth;

import com.aiaudit.platform.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public RefreshToken createRefreshToken(AppUser user) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiry))
                .build();
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken rotateToken(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.isRevoked()) {
            // Token reuse detected — revoke all tokens for this user
            refreshTokenRepository.revokeAllByUserId(existing.getUser().getId());
            throw new UnauthorizedException("Token reuse detected. All sessions revoked.");
        }

        if (existing.isExpired()) {
            throw new UnauthorizedException("Refresh token expired");
        }

        // Revoke old token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Issue new token
        return createRefreshToken(existing.getUser());
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
