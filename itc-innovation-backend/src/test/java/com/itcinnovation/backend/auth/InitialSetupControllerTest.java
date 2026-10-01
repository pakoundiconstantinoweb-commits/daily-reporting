package com.itcinnovation.backend.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.itcinnovation.backend.user.CreateManagerRequest;
import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

class InitialSetupControllerTest {

    @Test
    void createsMoreThanOneManagerWhenEmailsAreDifferent() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.existsByRole(UserRole.MANAGER)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InitialSetupController controller = new InitialSetupController(userRepository, passwordEncoder);

        controller.createInitialManager(new CreateManagerRequest(
                "First", "Manager", "first.manager@example.com", "password123", null));
        controller.createInitialManager(new CreateManagerRequest(
                "Second", "Manager", "second.manager@example.com", "password456", null));

        verify(userRepository).existsByEmailIgnoreCase("first.manager@example.com");
        verify(userRepository).existsByEmailIgnoreCase("second.manager@example.com");
        verify(userRepository, times(2)).save(any(User.class));
    }
}