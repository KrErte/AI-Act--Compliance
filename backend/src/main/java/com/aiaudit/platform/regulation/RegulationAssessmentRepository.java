package com.aiaudit.platform.regulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegulationAssessmentRepository extends JpaRepository<RegulationAssessment, UUID> {
    List<RegulationAssessment> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    Optional<RegulationAssessment> findTopByOrganizationIdAndRegulationIdOrderByCreatedAtDesc(UUID organizationId, UUID regulationId);
}
