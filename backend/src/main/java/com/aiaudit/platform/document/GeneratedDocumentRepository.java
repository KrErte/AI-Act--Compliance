package com.aiaudit.platform.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, UUID> {

    List<GeneratedDocument> findByAiSystemIdOrderByCreatedAtDesc(UUID aiSystemId);

    @Query("SELECT COUNT(d) FROM GeneratedDocument d WHERE d.aiSystem.organization.id = :orgId AND d.createdAt >= :since AND d.status = 'COMPLETED'")
    long countByOrganizationIdAndCreatedAtAfter(UUID orgId, Instant since);
}
