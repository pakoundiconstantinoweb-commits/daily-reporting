package com.itcinnovation.backend.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManagerInvitationService {

    private final ManagerInvitationRepository invitationRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ManagerInvitationService(ManagerInvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public ManagerInvitationCreatedResponse create(int expiresInHours, Long createdByUserId) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        ManagerInvitation invitation = new ManagerInvitation();
        invitation.setTokenHash(hashToken(token));
        invitation.setCreatedByUserId(createdByUserId);
        invitation.setExpiresAt(Instant.now().plus(expiresInHours, ChronoUnit.HOURS));
        invitationRepository.save(invitation);
        return new ManagerInvitationCreatedResponse(token, invitation.getExpiresAt());
    }

    @Transactional
    public void consume(String token) {
        if (token == null || token.isBlank()) {
            throw invalidInvitation();
        }
        Instant now = Instant.now();
        ManagerInvitation invitation = invitationRepository.findUsableForUpdate(hashToken(token), now)
                .orElseThrow(ManagerInvitationService::invalidInvitation);
        invitation.setUsedAt(now);
        invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<ManagerInvitationResponse> list() {
        Instant now = Instant.now();
        return invitationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(invitation -> ManagerInvitationResponse.from(invitation, now))
                .toList();
    }

    @Transactional
    public ManagerInvitationResponse revoke(Long invitationId) {
        ManagerInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation introuvable."));
        Instant now = Instant.now();
        if (ManagerInvitationResponse.from(invitation, now).status().equals("ACTIVE")) {
            invitation.setRevokedAt(now);
            invitationRepository.save(invitation);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cette invitation n’est plus active et ne peut pas être révoquée.");
        }
        return ManagerInvitationResponse.from(invitation, now);
    }

    static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static ResponseStatusException invalidInvitation() {
        return new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Cette invitation est invalide, expirée, révoquée ou déjà utilisée.");
    }
}