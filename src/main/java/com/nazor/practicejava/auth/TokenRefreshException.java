package com.nazor.practicejava.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a refresh token is missing, unknown, expired, or revoked. Mapped to
 * {@code 403 Forbidden} so the client knows it must sign in again.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for token [%s]: %s", token, message));
    }
}
