package com.itcinnovation.backend.user;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByRole(UserRole role);

    List<User> findByManager_IdAndRole(Long managerId, UserRole role);

    Optional<User> findByIdAndManager_IdAndRole(Long id, Long managerId, UserRole role);

    void deleteByManager_IdAndRole(Long managerId, UserRole role);

    @Query("""
            select coalesce(account.demoExpiresAt, manager.demoExpiresAt)
            from User account left join account.manager manager
            where account.id = :userId
            """)
    Optional<Instant> findDemoExpiresAtForUser(@Param("userId") Long userId);

    @Query("""
            select account.id from User account
            where account.demoExpiresAt is not null and account.demoExpiresAt <= :now
            """)
    List<Long> findExpiredDemoManagerIds(@Param("now") Instant now);
}
