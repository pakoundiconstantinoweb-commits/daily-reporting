package com.itcinnovation.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserResponse;

import jakarta.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AuthenticatedUserService authenticatedUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(
            AuthenticatedUserService authenticatedUserService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticatedUserService = authenticatedUserService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public UserResponse currentProfile(Authentication authentication) {
        return UserResponse.from(currentUser(authentication));
    }

    @PutMapping("/me")
    public UserResponse updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée.");
                });

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(normalizeOptional(request.phone()));
        user.setDepartment(normalizeOptional(request.department()));
        return UserResponse.from(userRepository.save(user));
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe est incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User currentUser(Authentication authentication) {
        return authenticatedUserService.requireActive(authentication);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
