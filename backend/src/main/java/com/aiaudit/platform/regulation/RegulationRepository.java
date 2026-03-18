package com.aiaudit.platform.regulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegulationRepository extends JpaRepository<Regulation, UUID> {
    Optional<Regulation> findByCode(String code);
}
