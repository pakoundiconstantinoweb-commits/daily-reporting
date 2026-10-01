package com.itcinnovation.backend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ManagerBootstrap {

    @Bean
    CommandLineRunner createInitialManager(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-manager.email:}") String email,
            @Value("${app.bootstrap-manager.password:}") String password,
            @Value("${app.bootstrap-manager.first-name:}") String firstName,
            @Value("${app.bootstrap-manager.last-name:}") String lastName) {
        return args -> {
            if (email.isBlank() || password.isBlank() || userRepository.count() > 0) {
                return;
            }

            User manager = new User();
            manager.setFirstName(firstName.isBlank() ? "Manager" : firstName);
            manager.setLastName(lastName.isBlank() ? "Entreprise" : lastName);
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode(password));
            manager.setRole(UserRole.MANAGER);
            manager.setStatus(UserStatus.ACTIVE);
            userRepository.save(manager);
        };
    }
}
