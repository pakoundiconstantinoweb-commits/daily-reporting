package com.itcinnovation.backend.managerinvite;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup/invitations")
public class PublicManagerSignupInvitationController {

    private final ManagerSignupInvitationService invitationService;

    public PublicManagerSignupInvitationController(ManagerSignupInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/{token}")
    public ManagerSignupInvitationValidationResponse validate(@PathVariable String token) {
        return invitationService.validate(token);
    }
}
