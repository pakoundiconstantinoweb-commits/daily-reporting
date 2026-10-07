package com.itcinnovation.backend.managerinvite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

class ManagerSignupInvitationServiceTest {

    @Test
    void createsAnOpaqueTokenAndStoresOnlyItsHash() {
        ManagerSignupInvitationRepository invitationRepository = mock(ManagerSignupInvitationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User superAdmin = new User();
        superAdmin.setRole(UserRole.SUPER_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));
        when(invitationRepository.save(any(ManagerSignupInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ManagerSignupInvitationService service = new ManagerSignupInvitationService(invitationRepository, userRepository);

        ManagerSignupInvitationCreatedResponse response = service.create(1L);

        assertEquals(43, response.token().length());
        assertNotNull(response.expiresAt());
        var saved = org.mockito.ArgumentCaptor.forClass(ManagerSignupInvitation.class);
        verify(invitationRepository).save(saved.capture());
        assertEquals(64, saved.getValue().getTokenHash().length());
        assertNotEquals(response.token(), saved.getValue().getTokenHash());
        assertEquals(superAdmin, saved.getValue().getCreatedBy());
    }

    @Test
    void consumesOnlyAnInvitationThatIsStillUsable() {
        ManagerSignupInvitationRepository invitationRepository = mock(ManagerSignupInvitationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ManagerSignupInvitation invitation = new ManagerSignupInvitation();
        invitation.setExpiresAt(Instant.now().plusSeconds(60));
        when(invitationRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(invitation));
        ManagerSignupInvitationService service = new ManagerSignupInvitationService(invitationRepository, userRepository);

        service.consume("valid-token");

        assertNotNull(invitation.getUsedAt());
        verify(invitationRepository).save(invitation);
    }

    @Test
    void rejectsExpiredInvitations() {
        ManagerSignupInvitationRepository invitationRepository = mock(ManagerSignupInvitationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ManagerSignupInvitation invitation = new ManagerSignupInvitation();
        invitation.setExpiresAt(Instant.now().minusSeconds(1));
        when(invitationRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(invitation));
        ManagerSignupInvitationService service = new ManagerSignupInvitationService(invitationRepository, userRepository);

        assertThrows(ResponseStatusException.class, () -> service.consume("expired-token"));
    }
}
