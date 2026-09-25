package com.itcinnovation.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        User user = userRepository.findById(Long.valueOf(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe est incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les nouveaux mots de passe ne correspondent pas");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nouveau mot de passe doit être différent");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
