package com.itcinnovation.backend.invitation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateManagerInvitationRequest(
        @NotNull @Min(1) @Max(168) Integer expiresInHours) {
}