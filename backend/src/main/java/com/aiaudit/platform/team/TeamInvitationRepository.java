package com.aiaudit.platform.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {

    Optional<TeamInvitation> findByToken(String token);

    List<TeamInvitation> findByOrganizationIdAndAcceptedAtIsNullOrderByCreatedAtDesc(UUID organizationId);

    boolean existsByEmailAndOrganizationIdAndAcceptedAtIsNull(String email, UUID organizationId);
}
