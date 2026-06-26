package com.nazor.practicejava.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Creates and validates the signed JWT access token and packages both the
 * access and refresh tokens into HttpOnly cookies.
 *
 * <p>The access token is a stateless JWT (subject = user email). The refresh
 * token is an opaque value persisted in the database (see {@code RefreshToken}),
 * which is what makes server-side revocation possible.
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtProperties properties;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
    }

    // ----- cookie extraction -----

    public String getJwtFromCookies(HttpServletRequest request) {
        return readCookie(request, properties.getCookieName());
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return readCookie(request, properties.getRefreshCookieName());
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie != null ? cookie.getValue() : null;
    }

    // ----- cookie creation -----

    public ResponseCookie generateJwtCookie(UserDetails userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return buildCookie(properties.getCookieName(), jwt, "/api", properties.getExpirationMs());
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return buildCookie(properties.getRefreshCookieName(), refreshToken,
                "/api/auth/refreshtoken", properties.getRefreshExpirationMs());
    }

    /** A maxAge-0 cookie that instructs the browser to drop the access token. */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(properties.getCookieName(), "")
                .path("/api").maxAge(0).httpOnly(true).build();
    }

    /** A maxAge-0 cookie that instructs the browser to drop the refresh token. */
    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(properties.getRefreshCookieName(), "")
                .path("/api/auth/refreshtoken").maxAge(0).httpOnly(true).build();
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeMs) {
        return ResponseCookie.from(name, value)
                .path(path)
                .maxAge(maxAgeMs / 1000)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
    }

    // ----- token lifecycle -----

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.getExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }
}
