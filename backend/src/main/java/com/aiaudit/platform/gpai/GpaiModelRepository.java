package com.aiaudit.platform.gpai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GpaiModelRepository extends JpaRepository<GpaiModel, UUID> {

    List<GpaiModel> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    Optional<GpaiModel> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
