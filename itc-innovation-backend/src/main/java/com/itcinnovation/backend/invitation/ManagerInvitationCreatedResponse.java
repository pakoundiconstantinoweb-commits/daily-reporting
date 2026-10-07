package com.itcinnovation.backend.invitation;

import java.time.Instant;

public record ManagerInvitationCreatedResponse(String token, Instant expiresAt) {
}