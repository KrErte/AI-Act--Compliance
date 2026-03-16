package com.aiaudit.platform.gpai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GpaiObligationRepository extends JpaRepository<GpaiObligation, UUID> {

    List<GpaiObligation> findByGpaiModelIdOrderBySortOrder(UUID gpaiModelId);

    void deleteAllByGpaiModelId(UUID gpaiModelId);
}
