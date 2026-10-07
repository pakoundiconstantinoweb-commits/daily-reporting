package com.itcinnovation.backend.auth;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
import com.itcinnovation.backend.managerinvite.ManagerSignupInvitationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setup")
public class InitialSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManagerSignupInvitationService invitationService;

    public InitialSetupController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ManagerSignupInvitationService invitationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationService = invitationService;
    }

    @PostMapping("/manager")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserResponse createManager(@Valid @RequestBody CreateManagerRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isPresent()) {
            String message = existingUser.get().getRole().canManageTeam()
                    ? "Un compte Directeur / Manager existe déjà avec cette adresse e-mail."
                    : "Cette adresse e-mail est déjà utilisée par un autre compte.";
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }

        invitationService.consume(request.invitationToken());
        User manager = new User();
        manager.setFirstName(request.firstName().trim());
        manager.setLastName(request.lastName().trim());
        manager.setEmail(email);
        manager.setPassword(passwordEncoder.encode(request.password()));
        manager.setPhone(normalizeOptional(request.phone()));
        manager.setRole(UserRole.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        return UserResponse.from(userRepository.save(manager));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
