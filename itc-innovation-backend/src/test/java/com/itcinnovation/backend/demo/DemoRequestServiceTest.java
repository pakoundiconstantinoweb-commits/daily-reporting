package com.itcinnovation.backend.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.reporting.ReportingRepository;
import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

class DemoRequestServiceTest {

    @Test
    void createsEmptyDemoWorkspaceAndKeepsContactRequest() {
        DemoRequestRepository requestRepository = mock(DemoRequestRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReportingRepository reportingRepository = mock(ReportingRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.existsByEmailIgnoreCase("director@example.com")).thenReturn(false);
        when(requestRepository.existsByEmailIgnoreCaseAndDemoExpiresAtAfter(
                anyString(), any(Instant.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "hashed:" + invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        DemoRequestService service = new DemoRequestService(
                requestRepository, userRepository, passwordEncoder);
        DemoCredentialsResponse response = service.create(
                new DemoRequestForm("Aïcha Mensah", " Director@Example.com ", "+228 90 00 00 00", true));

        assertEquals("director@example.com", response.email());
        assertFalse(response.password().isBlank());
        assertTrue(response.demoExpiresAt().isAfter(Instant.now()));
        ArgumentCaptor<User> users = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(users.capture());
        User manager = users.getValue();
        assertEquals(UserRole.MANAGER, manager.getRole());
        assertEquals("director@example.com", manager.getEmail());
        assertNotEquals(response.password(), manager.getPassword());
        verify(reportingRepository, org.mockito.Mockito.never()).save(any());
        verify(requestRepository, times(2)).save(any(DemoRequest.class));
    }

    @Test
    void rejectsAnEmailThatAlreadyHasAnAccount() {
        DemoRequestRepository requestRepository = mock(DemoRequestRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.existsByEmailIgnoreCase("director@example.com")).thenReturn(true);
        DemoRequestService service = new DemoRequestService(
                requestRepository, userRepository, mock(PasswordEncoder.class));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.create(new DemoRequestForm(
                        "Aïcha Mensah", "director@example.com", "+228 90 00 00 00", true)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}