package com.nazor.practicejava.user.dto;

import java.util.Set;

public record UpdateUserRequest(
        String fullName,
        String phoneNumber,
        Boolean enabled,
        Set<String> roles) {
}
