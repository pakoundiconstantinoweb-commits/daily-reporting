package com.itcinnovation.backend.auth;

import java.time.Instant;

import com.itcinnovation.backend.user.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        Instant demoExpiresAt
) {
}
