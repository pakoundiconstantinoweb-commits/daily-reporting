package com.itcinnovation.backend.managerinvite;

import java.time.Instant;

public record ManagerSignupInvitationCreatedResponse(Long id, String token, Instant expiresAt) {
}
