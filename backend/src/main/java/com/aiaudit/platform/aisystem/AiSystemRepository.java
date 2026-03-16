package com.aiaudit.platform.aisystem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AiSystemRepository extends JpaRepository<AiSystem, UUID>, JpaSpecificationExecutor<AiSystem> {

    Page<AiSystem> findByOrganizationIdAndDeletedFalse(UUID organizationId, Pageable pageable);

    Optional<AiSystem> findByIdAndOrganizationIdAndDeletedFalse(UUID id, UUID organizationId);

    long countByOrganizationIdAndDeletedFalse(UUID organizationId);

    @Query("SELECT COUNT(a) FROM AiSystem a WHERE a.organization.id = :orgId AND a.riskLevel = :riskLevel AND a.deleted = false")
    long countByOrganizationIdAndRiskLevel(UUID orgId, RiskLevel riskLevel);
}
