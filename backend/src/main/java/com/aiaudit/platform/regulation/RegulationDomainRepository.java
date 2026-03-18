package com.aiaudit.platform.regulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegulationDomainRepository extends JpaRepository<RegulationDomain, UUID> {
    List<RegulationDomain> findByRegulationIdOrderBySortOrder(UUID regulationId);
}
