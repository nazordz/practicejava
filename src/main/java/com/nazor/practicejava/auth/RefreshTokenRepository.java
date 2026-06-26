package com.nazor.practicejava.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Bulk-revokes every still-active refresh token for a user (used on sign-out).
     * Returns the number of rows affected.
     */
    @Modifying
    @Query("""
            update RefreshToken r
            set r.revoked = true, r.revokedAt = :now
            where r.user.id = :userId and r.revoked = false""")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
