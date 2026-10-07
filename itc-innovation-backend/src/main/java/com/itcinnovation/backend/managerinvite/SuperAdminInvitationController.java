package com.itcinnovation.backend.managerinvite;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/invitations")
public class SuperAdminInvitationController {

    private final ManagerSignupInvitationService invitationService;

    public SuperAdminInvitationController(ManagerSignupInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerSignupInvitationCreatedResponse create(Authentication authentication) {
        return invitationService.create(Long.valueOf(authentication.getName()));
    }

    @PatchMapping("/{id}/revoke")
    public ManagerSignupInvitationRevokedResponse revoke(
            @PathVariable Long id,
            Authentication authentication) {
        return invitationService.revoke(id, Long.valueOf(authentication.getName()));
    }

}
