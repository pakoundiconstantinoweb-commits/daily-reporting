package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.managerinvite.ManagerSignupInvitationService;
import com.itcinnovation.backend.user.CreateManagerRequest;
import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;
import com.itcinnovation.backend.user.UserStatus;

class InitialSetupControllerTest {

    @Test
    void createsAManagerAndConsumesItsInvitation() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ManagerSignupInvitationService invitationService = mock(ManagerSignupInvitationService.class);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InitialSetupController controller = new InitialSetupController(userRepository, passwordEncoder, invitationService);

        var created = controller.createManager(
                new CreateManagerRequest(" First ", " Manager ", "Manager@Example.com", "password123", "  ", "valid-token"));

        assertEquals("First", created.firstName());
        assertEquals("Manager", created.lastName());
        assertEquals("manager@example.com", created.email());
        assertEquals(UserRole.MANAGER, created.role());
        assertEquals(UserStatus.ACTIVE, created.status());
        verify(userRepository).findByEmailIgnoreCase("manager@example.com");
        verify(userRepository).save(any(User.class));
        verify(invitationService).consume("valid-token");
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void createsAnotherManagerWhenOtherManagersAlreadyExist() {
        UserRepository userRepository = mock(UserRepository.class);
        ManagerSignupInvitationService invitationService = mock(ManagerSignupInvitationService.class);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InitialSetupController controller = new InitialSetupController(
                userRepository, mock(PasswordEncoder.class), invitationService);

        var created = controller.createManager(request());

        assertEquals(UserRole.MANAGER, created.role());
        verify(userRepository).save(any(User.class));
        verify(invitationService).consume("valid-token");
    }

    @Test
    void refusesCreationAndExplainsWhenTheManagerEmailAlreadyExists() {
        UserRepository userRepository = mock(UserRepository.class);
        User existingManager = new User();
        existingManager.setRole(UserRole.MANAGER);
        when(userRepository.findByEmailIgnoreCase("manager@example.com")).thenReturn(Optional.of(existingManager));
        InitialSetupController controller = new InitialSetupController(
                userRepository, mock(PasswordEncoder.class), mock(ManagerSignupInvitationService.class));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.createManager(request()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Un compte Directeur / Manager existe déjà avec cette adresse e-mail.",
                exception.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    private static CreateManagerRequest request() {
        return new CreateManagerRequest("First", "Manager", "manager@example.com", "password123", null, "valid-token");
    }
}