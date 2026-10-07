package com.itcinnovation.backend.invitation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagerInvitationRepository extends JpaRepository<ManagerInvitation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select invitation from ManagerInvitation invitation
            where invitation.tokenHash = :tokenHash
              and invitation.usedAt is null
              and invitation.revokedAt is null
              and invitation.expiresAt > :now
            """)
    Optional<ManagerInvitation> findUsableForUpdate(
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now);

    List<ManagerInvitation> findAllByOrderByCreatedAtDesc();
}