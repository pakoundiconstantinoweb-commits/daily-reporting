package com.itcinnovation.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/manager/employees")
public class ManagerUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public java.util.List<UserResponse> listEmployees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.EMPLOYEE)
                .map(UserResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée");
        }

        User employee = new User();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPassword(passwordEncoder.encode(request.password()));
        employee.setPhone(request.phone());
        employee.setDepartment(request.department());
        employee.setRole(UserRole.EMPLOYEE);
        employee.setStatus(UserStatus.ACTIVE);

        return UserResponse.from(userRepository.save(employee));
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User employee = userRepository.findById(id)
                .filter(user -> user.getRole() == UserRole.EMPLOYEE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employé introuvable"));

        employee.setStatus(request.status());
        return UserResponse.from(userRepository.save(employee));
    }
}
