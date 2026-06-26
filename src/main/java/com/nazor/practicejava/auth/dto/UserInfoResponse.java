package com.nazor.practicejava.auth.dto;

import java.util.List;
import java.util.UUID;

/** Identity returned after a successful sign-in. Tokens travel in HttpOnly cookies. */
public record UserInfoResponse(
        UUID id,
        String email,
        List<String> roles) {
}
