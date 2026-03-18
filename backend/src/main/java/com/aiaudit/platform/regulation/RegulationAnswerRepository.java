package com.aiaudit.platform.regulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegulationAnswerRepository extends JpaRepository<RegulationAnswer, UUID> {
    List<RegulationAnswer> findByAssessmentId(UUID assessmentId);
}
