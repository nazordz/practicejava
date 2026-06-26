package com.nazor.practicejava.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Returns a JSON {@code 401 Unauthorized} body for unauthenticated requests
 * instead of triggering a browser HTTP Basic prompt.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Unauthorized request to {}: {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"%s","path":"%s"}"""
                .formatted(escape(authException.getMessage()), escape(request.getRequestURI())));
    }

    /** Minimal JSON string escaping for the values we emit. */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
