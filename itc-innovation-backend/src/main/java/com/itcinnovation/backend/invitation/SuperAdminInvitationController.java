package com.itcinnovation.backend.invitation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/super-admin/invitations")
public class SuperAdminInvitationController {

    private final ManagerInvitationService invitationService;

    public SuperAdminInvitationController(ManagerInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerInvitationCreatedResponse create(
            @Valid @RequestBody CreateManagerInvitationRequest request,
            Authentication authentication) {
        return invitationService.create(request.expiresInHours(), Long.valueOf(authentication.getName()));
    }

    @GetMapping
    public List<ManagerInvitationResponse> list() {
        return invitationService.list();
    }

    @PatchMapping("/{id}/revoke")
    public ManagerInvitationResponse revoke(@PathVariable Long id) {
        return invitationService.revoke(id);
    }
}