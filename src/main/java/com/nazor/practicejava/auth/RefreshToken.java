package com.nazor.practicejava.auth;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;

import com.nazor.practicejava.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A persisted, opaque refresh token. Keeping refresh tokens in the database (as
 * opposed to a self-contained JWT) is what allows them to be revoked: a token is
 * only accepted while {@link #revoked} is {@code false} and it has not expired.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Generated
    @ColumnDefault("uuidv7()")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /** When {@code true} the token can no longer be exchanged for a new access token. */
    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }
}
