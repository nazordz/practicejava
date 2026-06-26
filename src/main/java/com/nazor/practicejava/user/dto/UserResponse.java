package com.nazor.practicejava.user.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nazor.practicejava.user.Role;
import com.nazor.practicejava.user.User;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phoneNumber,
        boolean enabled,
        Set<String> roles,
        Instant emailVerifiedAt,
        Instant lastLoginAt,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet()),
                user.getEmailVerifiedAt(),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }
}
