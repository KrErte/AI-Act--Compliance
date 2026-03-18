package com.aiaudit.platform.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AppUser> findByPasswordResetToken(String token);

    List<AppUser> findByOrganizationIdOrderByCreatedAt(UUID organizationId);

    Page<AppUser> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    long countByOrganizationId(UUID organizationId);

    long countByOrganizationIdAndRole(UUID organizationId, UserRole role);

    @Query("SELECT u FROM AppUser u WHERE u.organization.id = :orgId AND u.enabled = true")
    List<AppUser> findActiveByOrganizationId(UUID orgId);
}
