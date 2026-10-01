package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserStatus;

class AuthControllerTest {

    @Test
    void returnsInactiveAccountMessageOnlyAfterPasswordIsVerified() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setStatus(UserStatus.INACTIVE);
        user.setPassword("stored-hash");
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmailIgnoreCase("employee@example.com")).thenReturn(Optional.of(user));
        AuthController controller = new AuthController(userRepository, passwordEncoder, mock(JwtEncoder.class));

        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);
        ResponseStatusException invalidPassword = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(new AuthRequest("employee@example.com", "wrong-password")));
        assertEquals(HttpStatus.UNAUTHORIZED, invalidPassword.getStatusCode());
        assertEquals("Email ou mot de passe incorrect", invalidPassword.getReason());

        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);
        ResponseStatusException inactiveAccount = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(new AuthRequest("employee@example.com", "correct-password")));
        assertEquals(HttpStatus.FORBIDDEN, inactiveAccount.getStatusCode());
        assertEquals(AuthenticatedUserService.INACTIVE_ACCOUNT_MESSAGE, inactiveAccount.getReason());
    }
}