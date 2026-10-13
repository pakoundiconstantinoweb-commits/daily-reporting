package com.itcinnovation.backend.demo;

import java.time.Instant;

public record DemoRequestResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Instant createdAt,
        Instant demoExpiresAt,
        boolean demoActive
) {

    public static DemoRequestResponse from(DemoRequest request, Instant now) {
        return new DemoRequestResponse(
                request.getId(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getCreatedAt(),
                request.getDemoExpiresAt(),
                request.getDemoExpiresAt().isAfter(now));
    }
}