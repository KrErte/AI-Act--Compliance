package com.aiaudit.platform.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AppUser> findByPasswordResetToken(String token);

    List<AppUser> findByOrganizationIdOrderByCreatedAt(UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
