package com.aiaudit.platform.alert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ComplianceAlertRepository extends JpaRepository<ComplianceAlert, UUID> {

    Page<ComplianceAlert> findByOrganizationIdAndDismissedFalseOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    long countByOrganizationIdAndReadFalseAndDismissedFalse(UUID organizationId);

    @Modifying
    @Query("UPDATE ComplianceAlert a SET a.read = true WHERE a.organization.id = :orgId AND a.read = false")
    void markAllAsRead(UUID orgId);
}
