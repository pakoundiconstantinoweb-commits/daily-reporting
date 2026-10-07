package com.itcinnovation.backend.invitation;

import java.time.Instant;

public record ManagerInvitationResponse(
        Long id,
        Instant createdAt,
        Instant expiresAt,
        Instant usedAt,
        Instant revokedAt,
        String status) {

    static ManagerInvitationResponse from(ManagerInvitation invitation, Instant now) {
        String status;
        if (invitation.getRevokedAt() != null) {
            status = "REVOKED";
        } else if (invitation.getUsedAt() != null) {
            status = "USED";
        } else if (!invitation.getExpiresAt().isAfter(now)) {
            status = "EXPIRED";
        } else {
            status = "ACTIVE";
        }
        return new ManagerInvitationResponse(
                invitation.getId(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                invitation.getUsedAt(),
                invitation.getRevokedAt(),
                status);
    }
}