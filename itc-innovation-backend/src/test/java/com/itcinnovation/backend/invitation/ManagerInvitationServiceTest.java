package com.itcinnovation.backend.invitation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ManagerInvitationServiceTest {

    @Test
    void createsInvitationWithOnlyItsHashStored() {
        ManagerInvitationRepository repository = mock(ManagerInvitationRepository.class);
        when(repository.save(org.mockito.ArgumentMatchers.any(ManagerInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ManagerInvitationService service = new ManagerInvitationService(repository);

        ManagerInvitationCreatedResponse created = service.create(24, 7L);

        assertEquals(43, created.token().length());
        assertEquals(64, ManagerInvitationService.hashToken(created.token()).length());
        assertNotEquals(created.token(), ManagerInvitationService.hashToken(created.token()));
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(invitation ->
                invitation.getCreatedByUserId().equals(7L)
                        && invitation.getTokenHash().equals(ManagerInvitationService.hashToken(created.token()))
                        && invitation.getExpiresAt().isAfter(Instant.now())));
    }

    @Test
    void consumesOnlyValidInvitationAndMarksItUsed() {
        ManagerInvitationRepository repository = mock(ManagerInvitationRepository.class);
        ManagerInvitation invitation = new ManagerInvitation();
        when(repository.findUsableForUpdate(
                eq(ManagerInvitationService.hashToken("temporary-token")),
                org.mockito.ArgumentMatchers.any(Instant.class))).thenReturn(Optional.of(invitation));
        when(repository.save(invitation)).thenReturn(invitation);

        new ManagerInvitationService(repository).consume("temporary-token");

        assertTrue(invitation.getUsedAt() != null);
        verify(repository).save(invitation);
    }

    @Test
    void rejectsMissingOrInvalidInvitation() {
        ManagerInvitationRepository repository = mock(ManagerInvitationRepository.class);
        ManagerInvitationService service = new ManagerInvitationService(repository);

        ResponseStatusException missing = assertThrows(ResponseStatusException.class, () -> service.consume(" "));

        assertEquals(HttpStatus.FORBIDDEN, missing.getStatusCode());
        verify(repository, never()).findUsableForUpdate(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Instant.class));
    }
}