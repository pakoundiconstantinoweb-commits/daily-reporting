package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserStatus;

class AuthenticatedUserServiceTest {

    @Test
    void rejectsAnInactiveUserOnProtectedRequests() {
        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("42");
        User user = new User();
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new AuthenticatedUserService(userRepository).requireActive(authentication));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(AuthenticatedUserService.INACTIVE_ACCOUNT_MESSAGE, exception.getReason());
    }
}