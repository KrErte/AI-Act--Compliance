package com.aiaudit.platform.classification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ClassificationResponseRepository extends JpaRepository<ClassificationResponse, UUID> {

    List<ClassificationResponse> findByAiSystemId(UUID aiSystemId);

    @Modifying
    @Query("DELETE FROM ClassificationResponse cr WHERE cr.aiSystem.id = :aiSystemId")
    void deleteAllByAiSystemId(UUID aiSystemId);
}
