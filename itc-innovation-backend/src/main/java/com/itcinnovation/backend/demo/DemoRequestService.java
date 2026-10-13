package com.itcinnovation.backend.demo;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;
import com.itcinnovation.backend.user.UserStatus;

@Service
public class DemoRequestService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final DemoRequestRepository demoRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoRequestService(
            DemoRequestRepository demoRequestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.demoRequestRepository = demoRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DemoCredentialsResponse create(DemoRequestForm form) {
        Instant now = Instant.now();
        String email = form.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)
                || demoRequestRepository.existsByEmailIgnoreCaseAndDemoExpiresAtAfter(email, now)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Une démo ou un compte existe déjà pour cette adresse e-mail.");
        }

        Instant expiresAt = now.plusSeconds(24 * 60 * 60);
        DemoRequest request = new DemoRequest();
        request.setFullName(form.fullName().trim());
        request.setEmail(email);
        request.setPhone(form.phone().trim());
        request.setCreatedAt(now);
        request.setConsentedAt(now);
        request.setDemoExpiresAt(expiresAt);
        demoRequestRepository.save(request);

        String password = generatePassword();
        String[] names = splitName(form.fullName());
        User manager = new User();
        manager.setFirstName(names[0]);
        manager.setLastName(names[1]);
        manager.setEmail(email);
        manager.setPassword(passwordEncoder.encode(password));
        manager.setPhone(form.phone().trim());
        manager.setRole(UserRole.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        manager.setDemoExpiresAt(expiresAt);
        manager = userRepository.save(manager);
        request.setDemoManagerId(manager.getId());
        demoRequestRepository.save(request);
        return new DemoCredentialsResponse(email, password, expiresAt);
    }

    @Transactional(readOnly = true)
    public List<DemoRequestResponse> listRequests() {
        Instant now = Instant.now();
        return demoRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(request -> DemoRequestResponse.from(request, now))
                .toList();
    }

    private static String[] splitName(String fullName) {
        String[] names = fullName.trim().split("\\s+", 2);
        return names.length == 2 ? names : new String[] { names[0], "Démo" };
    }

    private static String generatePassword() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}