package com.aiaudit.platform.audit;

import com.aiaudit.platform.audit.dto.AuditLogDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.organization.Organization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, UUID entityId, String action,
                    String oldValue, String newValue,
                    Map<String, Object> metadata,
                    AppUser user, Organization organization) {
        try {
            AuditLog entry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .metadata(metadata)
                    .user(user)
                    .organization(organization)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: {} {} {}", entityType, entityId, action, e);
        }
    }

    public void log(String entityType, UUID entityId, String action,
                    AppUser user, Organization organization) {
        log(entityType, entityId, action, null, null, null, user, organization);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLog(UUID organizationId, int page, int size) {
        Page<AuditLogDto> result = auditLogRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(page, size))
                .map(AuditLogDto::from);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getEntityAuditLog(String entityType, UUID entityId, int page, int size) {
        Page<AuditLogDto> result = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, PageRequest.of(page, size))
                .map(AuditLogDto::from);
        return PagedResponse.from(result);
    }
}
