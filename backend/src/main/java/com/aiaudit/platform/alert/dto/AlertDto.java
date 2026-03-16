package com.aiaudit.platform.alert.dto;

import com.aiaudit.platform.alert.ComplianceAlert;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AlertDto {
    private UUID id;
    private String alertType;
    private String title;
    private String message;
    private String severity;
    private boolean read;
    private String source;
    private String linkUrl;
    private UUID aiSystemId;
    private String aiSystemName;
    private Instant createdAt;

    public static AlertDto from(ComplianceAlert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().name())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .severity(alert.getSeverity().name())
                .read(alert.isRead())
                .source(alert.getSource())
                .linkUrl(alert.getLinkUrl())
                .aiSystemId(alert.getAiSystem() != null ? alert.getAiSystem().getId() : null)
                .aiSystemName(alert.getAiSystem() != null ? alert.getAiSystem().getName() : null)
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
