package com.nazor.practicejava.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nazor.practicejava.security.JwtProperties;
import com.nazor.practicejava.user.User;

/**
 * Issues, looks up, verifies, and revokes persisted refresh tokens. Verification
 * enforces both expiry and the {@code revoked} flag, which is the server-side
 * revocation mechanism.
 */
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()));
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Returns the token if it is still usable; otherwise revokes it (when expired)
     * and throws {@link TokenRefreshException}.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was revoked. Please sign in again.");
        }
        if (token.isExpired()) {
            token.revoke();
            refreshTokenRepository.save(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token has expired. Please sign in again.");
        }
        return token;
    }

    /** Revokes every active refresh token for a user (sign-out / "log out everywhere"). */
    public int revokeByUser(User user) {
        return refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
    }
}
