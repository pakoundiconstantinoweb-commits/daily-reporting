package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;
import com.itcinnovation.backend.user.UserStatus;
import java.util.Optional;

class AccountControllerTest {

    @Test
    void returnsAuthenticatedUserProfileWithoutPassword() {
        AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
        User user = new User();
        user.setId(7L);
        user.setFirstName("Awa");
        user.setLastName("Mensah");
        user.setEmail("awa@example.com");
        user.setPhone("+228 90000000");
        user.setDepartment("Informatique");
        user.setRole(UserRole.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        Authentication authentication = mock(Authentication.class);
        when(authenticatedUserService.requireActive(authentication)).thenReturn(user);

        AccountController controller = new AccountController(
                authenticatedUserService, mock(UserRepository.class), mock(PasswordEncoder.class));

        var profile = controller.currentProfile(authentication);

        assertEquals(7L, profile.id());
        assertEquals("Awa", profile.firstName());
        assertEquals("Mensah", profile.lastName());
        assertEquals("awa@example.com", profile.email());
        assertEquals("+228 90000000", profile.phone());
        assertEquals("Informatique", profile.department());
        assertEquals(UserRole.EMPLOYEE, profile.role());
        assertEquals(UserStatus.ACTIVE, profile.status());
    }

    @Test
    void updatesAuthenticatedProfileAndNormalizesOptionalFields() {
        AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(7L);
        user.setFirstName("Awa");
        user.setLastName("Mensah");
        user.setEmail("awa@example.com");
        user.setRole(UserRole.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        Authentication authentication = mock(Authentication.class);
        when(authenticatedUserService.requireActive(authentication)).thenReturn(user);
        when(userRepository.findByEmailIgnoreCase("awa.nouveau@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        AccountController controller = new AccountController(
                authenticatedUserService, userRepository, mock(PasswordEncoder.class));

        var updated = controller.updateProfile(
                new ProfileUpdateRequest(" Awa ", " Mensah ", " Awa.Nouveau@Example.com ", "  ", " Informatique "),
                authentication);

        assertEquals("Awa", updated.firstName());
        assertEquals("Mensah", updated.lastName());
        assertEquals("awa.nouveau@example.com", updated.email());
        assertNull(updated.phone());
        assertEquals("Informatique", updated.department());
        assertEquals(UserRole.EMPLOYEE, updated.role());
        assertEquals(UserStatus.ACTIVE, updated.status());
    }
}
