package com.aiaudit.platform.audit.dto;

import com.aiaudit.platform.audit.AuditLog;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AuditLogDto {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private Map<String, Object> metadata;
    private String userName;
    private Instant createdAt;

    public static AuditLogDto from(AuditLog log) {
        String userName = null;
        if (log.getUser() != null) {
            userName = log.getUser().getFirstName() + " " + log.getUser().getLastName();
        }
        return AuditLogDto.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .metadata(log.getMetadata())
                .userName(userName)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
