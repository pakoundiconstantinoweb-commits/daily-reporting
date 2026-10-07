package com.itcinnovation.backend.managerinvite;

import java.time.Instant;

public record ManagerSignupInvitationValidationResponse(boolean valid, Instant expiresAt) {
}
