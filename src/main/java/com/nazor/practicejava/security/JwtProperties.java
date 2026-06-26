package com.nazor.practicejava.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Binds the {@code practicejava.jwt.*} configuration that drives token signing
 * and the HttpOnly cookies used to carry the access and refresh tokens.
 */
@Component
@ConfigurationProperties(prefix = "practicejava.jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded secret used to sign and verify access tokens (HMAC-SHA).
     * The default lets the app boot for local development; override it in real
     * deployments via {@code PRACTICEJAVA_JWT_SECRET} (or an untracked
     * {@code application.yml}). See {@code application-example.yml}.
     */
    private String secret =
            "rtibpSh/hDZIYu8/6vyI2fvHL6+MkfuVf/KKaDguoqQhCAkgvWqdd7OmEBT2vIUaeEffPEydJPYef1R54gvYPQ==";

    /** Cookie that carries the short-lived JWT access token. */
    private String cookieName = "practicejava-jwt";

    /** Cookie that carries the long-lived opaque refresh token. */
    private String refreshCookieName = "practicejava-refresh-jwt";

    /** Access token validity, in milliseconds (default 1 hour). */
    private long expirationMs = 3_600_000L;

    /** Refresh token validity, in milliseconds (default 24 hours). */
    private long refreshExpirationMs = 86_400_000L;
}
