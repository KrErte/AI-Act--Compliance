package com.aiaudit.platform.classification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassificationQuestionRepository extends JpaRepository<ClassificationQuestion, UUID> {

    List<ClassificationQuestion> findByActiveTrueOrderBySortOrder();

    Optional<ClassificationQuestion> findByQuestionKey(String questionKey);
}
