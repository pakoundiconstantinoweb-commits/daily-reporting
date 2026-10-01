package com.itcinnovation.backend.user;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(UserRole role);

    List<User> findByManager_IdAndRole(Long managerId, UserRole role);

    Optional<User> findByIdAndManager_IdAndRole(Long id, Long managerId, UserRole role);
}
