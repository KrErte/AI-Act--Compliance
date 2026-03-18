package com.aiaudit.platform.regulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegulationQuestionRepository extends JpaRepository<RegulationQuestion, UUID> {
    List<RegulationQuestion> findByDomainIdOrderBySortOrder(UUID domainId);
    long countByDomainId(UUID domainId);
}
