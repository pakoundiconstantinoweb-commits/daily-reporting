package com.itcinnovation.backend.demo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {

    boolean existsByEmailIgnoreCaseAndDemoExpiresAtAfter(String email, Instant now);

    List<DemoRequest> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("update DemoRequest request set request.demoManagerId = null where request.demoManagerId = :managerId")
    int clearDemoManagerId(@Param("managerId") Long managerId);
}