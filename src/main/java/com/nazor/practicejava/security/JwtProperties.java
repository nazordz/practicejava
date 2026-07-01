package com.nazor.practicejava.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Binds the {@code practicejava.jwt.*} configuration that drives token signing
 * and the HttpOnly cookies used to carry the access and refresh tokens.
 */
@Component
@ConfigurationProperties(prefix = "practicejava.jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded secret used to sign and verify access tokens (HMAC-SHA).
     * Supply it via {@code PRACTICEJAVA_JWT_SECRET} or an untracked
     * {@code application.yml}. See {@code application-example.yml}.
     */
    @NotBlank
    private String secret;

    /** Cookie that carries the short-lived JWT access token. */
    @NotBlank
    private String cookieName;

    /** Cookie that carries the long-lived opaque refresh token. */
    @NotBlank
    private String refreshCookieName;

    /** Access token validity, in milliseconds. */
    @Positive
    private long expirationMs;

    /** Refresh token validity, in milliseconds. */
    @Positive
    private long refreshExpirationMs;
}
