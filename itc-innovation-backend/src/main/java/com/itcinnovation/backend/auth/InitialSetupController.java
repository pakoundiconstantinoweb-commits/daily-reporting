package com.itcinnovation.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.CreateManagerRequest;
import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserResponse;
import com.itcinnovation.backend.user.UserRole;
import com.itcinnovation.backend.user.UserStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setup")
public class InitialSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialSetupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/manager")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createInitialManager(@Valid @RequestBody CreateManagerRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée");
        }

        User manager = new User();
        manager.setFirstName(request.firstName());
        manager.setLastName(request.lastName());
        manager.setEmail(request.email());
        manager.setPassword(passwordEncoder.encode(request.password()));
        manager.setPhone(request.phone());
        manager.setRole(UserRole.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        return UserResponse.from(userRepository.save(manager));
    }
}
