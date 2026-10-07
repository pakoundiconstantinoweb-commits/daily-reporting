package com.itcinnovation.backend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Configuration
public class ManagerBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerBootstrap.class);

    @Bean
    @Order(0)
    CommandLineRunner allowSuperAdminUserRole(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("alter table users drop constraint if exists users_role_check");
            jdbcTemplate.execute("""
                    alter table users
                    add constraint users_role_check
                    check (role in ('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN'))
                    """);
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner createInitialSuperAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-super-admin.email:}") String configuredEmail,
            @Value("${app.bootstrap-super-admin.password:}") String password,
            @Value("${app.bootstrap-super-admin.first-name:Super}") String firstName,
            @Value("${app.bootstrap-super-admin.last-name:Admin}") String lastName) {
        return args -> {
            if (configuredEmail.isBlank() && password.isBlank()) {
                LOGGER.warn("Super-admin bootstrap is disabled; configure SUPER_ADMIN_EMAIL and SUPER_ADMIN_PASSWORD.");
                return;
            }
            if (configuredEmail.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                        "Both SUPER_ADMIN_EMAIL and SUPER_ADMIN_PASSWORD must be configured together.");
            }

            String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
            var existingUser = userRepository.findByEmailIgnoreCase(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getRole() == UserRole.SUPER_ADMIN) {
                    return;
                }
                if (user.getRole() != UserRole.MANAGER) {
                    throw new IllegalStateException(
                            "Configured super-admin email already belongs to a non-manager account.");
                }

                user.setRole(UserRole.SUPER_ADMIN);
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                LOGGER.info("Promoted the configured manager account to super-admin.");
                return;
            }

            User superAdmin = new User();
            superAdmin.setFirstName(firstName.isBlank() ? "Super" : firstName.trim());
            superAdmin.setLastName(lastName.isBlank() ? "Admin" : lastName.trim());
            superAdmin.setEmail(email);
            superAdmin.setPassword(passwordEncoder.encode(password));
            superAdmin.setRole(UserRole.SUPER_ADMIN);
            superAdmin.setStatus(UserStatus.ACTIVE);
            userRepository.save(superAdmin);
            LOGGER.info("Created the configured super-admin account.");
        };
    }
}
