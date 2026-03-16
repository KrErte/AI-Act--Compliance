package com.aiaudit.platform.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplianceObligationRepository extends JpaRepository<ComplianceObligation, UUID> {

    List<ComplianceObligation> findByAiSystemIdOrderBySortOrder(UUID aiSystemId);

    Optional<ComplianceObligation> findByIdAndAiSystemId(UUID id, UUID aiSystemId);

    @Modifying
    @Query("DELETE FROM ComplianceObligation co WHERE co.aiSystem.id = :aiSystemId")
    void deleteAllByAiSystemId(UUID aiSystemId);

    long countByAiSystemId(UUID aiSystemId);

    long countByAiSystemIdAndStatus(UUID aiSystemId, ObligationStatus status);

    @Query("SELECT co FROM ComplianceObligation co WHERE co.aiSystem.organization.id = :orgId AND co.dueDate IS NOT NULL ORDER BY co.dueDate ASC")
    List<ComplianceObligation> findUpcomingByOrganizationId(UUID orgId);

    @Query("SELECT co FROM ComplianceObligation co JOIN FETCH co.aiSystem WHERE co.assignedTo.id = :userId ORDER BY co.dueDate ASC NULLS LAST")
    List<ComplianceObligation> findByAssignedToIdOrderByDueDateAsc(UUID userId);
}
