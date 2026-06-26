package com.nazor.practicejava.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credentials submitted to {@code POST /api/auth/signin}. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
