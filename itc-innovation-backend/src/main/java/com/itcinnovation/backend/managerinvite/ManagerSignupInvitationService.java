package com.itcinnovation.backend.managerinvite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

@Service
public class ManagerSignupInvitationService {

    private static final Duration INVITATION_LIFETIME = Duration.ofHours(24);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ManagerSignupInvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public ManagerSignupInvitationService(
            ManagerSignupInvitationRepository invitationRepository,
            UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ManagerSignupInvitationCreatedResponse create(Long superAdminId) {
        User creator = userRepository.findById(superAdminId)
                .filter(user -> user.getRole() == UserRole.SUPER_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Compte super-administrateur requis."));

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        Instant now = Instant.now();

        ManagerSignupInvitation invitation = new ManagerSignupInvitation();
        invitation.setTokenHash(hashToken(token));
        invitation.setCreatedAt(now);
        invitation.setExpiresAt(now.plus(INVITATION_LIFETIME));
        invitation.setCreatedBy(creator);
        ManagerSignupInvitation saved = invitationRepository.save(invitation);
        return new ManagerSignupInvitationCreatedResponse(saved.getId(), token, saved.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public ManagerSignupInvitationValidationResponse validate(String token) {
        ManagerSignupInvitation invitation = invitationRepository.findByTokenHash(hashToken(token))
                .orElseThrow(ManagerSignupInvitationService::invalidInvitation);
        ensureUsable(invitation, Instant.now());
        return new ManagerSignupInvitationValidationResponse(true, invitation.getExpiresAt());
    }

    @Transactional
    public void consume(String token) {
        Instant now = Instant.now();
        ManagerSignupInvitation invitation = invitationRepository.findByTokenHashForUpdate(hashToken(token))
                .orElseThrow(ManagerSignupInvitationService::invalidInvitation);
        ensureUsable(invitation, now);
        invitation.setUsedAt(now);
        invitationRepository.save(invitation);
    }

    @Transactional
    public ManagerSignupInvitationRevokedResponse revoke(Long invitationId, Long superAdminId) {
        userRepository.findById(superAdminId)
                .filter(user -> user.getRole() == UserRole.SUPER_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Compte super-administrateur requis."));
        ManagerSignupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation introuvable."));
        ensureUsable(invitation, Instant.now());
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
        return new ManagerSignupInvitationRevokedResponse(true);
    }

    private static void ensureUsable(ManagerSignupInvitation invitation, Instant now) {
        if (invitation.getUsedAt() != null || invitation.getRevokedAt() != null
                || !invitation.getExpiresAt().isAfter(now)) {
            throw invalidInvitation();
        }
    }

    private static ResponseStatusException invalidInvitation() {
        return new ResponseStatusException(
                HttpStatus.GONE, "Cette invitation est invalide, expirée ou déjà utilisée.");
    }

    private static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw invalidInvitation();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
