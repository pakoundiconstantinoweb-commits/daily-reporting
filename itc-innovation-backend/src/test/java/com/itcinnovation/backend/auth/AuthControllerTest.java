package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;
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
        AuthController controller = new AuthController(userRepository, passwordEncoder, mock(JwtEncoder.class), "");

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

    @Test
    void grantsSuperAdminRoleOnlyToTheConfiguredAccount() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = new User();
        user.setId(42L);
        user.setFirstName("Super");
        user.setLastName("Admin");
        user.setEmail("admin@example.com");
        user.setRole(UserRole.MANAGER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword("stored-hash");
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("signed-token")
                        .header("alg", "HS256")
                        .subject("42")
                        .claim("role", "SUPER_ADMIN")
                        .build());

        AuthController controller = new AuthController(
                userRepository, passwordEncoder, jwtEncoder, "ADMIN@example.com");

        AuthResponse response = controller.login(new AuthRequest("admin@example.com", "correct-password"));

        assertEquals(UserRole.SUPER_ADMIN, response.role());
    }
}