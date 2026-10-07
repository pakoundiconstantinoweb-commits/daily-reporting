package com.itcinnovation.backend.managerinvite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ManagerSignupInvitationRepository extends JpaRepository<ManagerSignupInvitation, Long> {

    Optional<ManagerSignupInvitation> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invitation from ManagerSignupInvitation invitation where invitation.tokenHash = :tokenHash")
    Optional<ManagerSignupInvitation> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
