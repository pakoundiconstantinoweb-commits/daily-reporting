package com.itcinnovation.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class ManagerUserControllerTest {

    @Test
    void resetsEmployeePasswordWithoutOldPasswordForOwnedEmployee() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Authentication authentication = mock(Authentication.class);
        User manager = user(10L, UserRole.MANAGER);
        User employee = user(20L, UserRole.EMPLOYEE);
        when(authentication.getName()).thenReturn("10");
        when(repository.findById(10L)).thenReturn(Optional.of(manager));
        when(repository.findByIdAndManager_IdAndRole(20L, 10L, UserRole.EMPLOYEE))
                .thenReturn(Optional.of(employee));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        ManagerUserController controller = new ManagerUserController(repository, passwordEncoder);
        controller.resetEmployeePassword(20L, new ResetEmployeePasswordRequest("new-password"), authentication);

        assertEquals("encoded-password", employee.getPassword());
        verify(passwordEncoder).encode("new-password");
        verify(repository).save(employee);
    }

    @Test
    void refusesPasswordResetForEmployeeOutsideAuthenticatedManagersTeam() {
        UserRepository repository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);
        User manager = user(10L, UserRole.MANAGER);
        when(authentication.getName()).thenReturn("10");
        when(repository.findById(10L)).thenReturn(Optional.of(manager));
        when(repository.findByIdAndManager_IdAndRole(20L, 10L, UserRole.EMPLOYEE))
                .thenReturn(Optional.empty());

        ManagerUserController controller = new ManagerUserController(repository, mock(PasswordEncoder.class));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.resetEmployeePassword(
                        20L, new ResetEmployeePasswordRequest("new-password"), authentication));

        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updatesOnlyEmployeeOwnedByAuthenticatedManager() {
        UserRepository repository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);
        User manager = user(10L, UserRole.MANAGER);
        User employee = user(20L, UserRole.EMPLOYEE);
        employee.setManager(manager);
        when(authentication.getName()).thenReturn("10");
        when(repository.findById(10L)).thenReturn(Optional.of(manager));
        when(repository.findByIdAndManager_IdAndRole(20L, 10L, UserRole.EMPLOYEE))
                .thenReturn(Optional.of(employee));
        when(repository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 20L)).thenReturn(false);
        when(repository.save(employee)).thenReturn(employee);

        ManagerUserController controller = new ManagerUserController(repository, mock(PasswordEncoder.class));
        UserResponse response = controller.updateEmployee(
                20L,
                new UpdateEmployeeRequest(" Awa ", "Doe", "new@example.com", " ", "  IT  "),
                authentication);

        assertEquals("Awa", response.firstName());
        assertEquals("new@example.com", response.email());
        assertNull(response.phone());
        assertEquals("IT", response.department());
        verify(repository).findByIdAndManager_IdAndRole(20L, 10L, UserRole.EMPLOYEE);
    }

    @Test
    void refusesToUpdateEmployeeOutsideAuthenticatedManagersTeam() {
        UserRepository repository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);
        User manager = user(10L, UserRole.MANAGER);
        when(authentication.getName()).thenReturn("10");
        when(repository.findById(10L)).thenReturn(Optional.of(manager));
        when(repository.findByIdAndManager_IdAndRole(20L, 10L, UserRole.EMPLOYEE))
                .thenReturn(Optional.empty());

        ManagerUserController controller = new ManagerUserController(repository, mock(PasswordEncoder.class));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.updateEmployee(
                        20L,
                        new UpdateEmployeeRequest("Awa", "Doe", "new@example.com", null, null),
                        authentication));

        assertEquals(404, exception.getStatusCode().value());
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
