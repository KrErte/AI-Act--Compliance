package com.aiaudit.platform.aisystem.dto;

import com.aiaudit.platform.aisystem.AiSystem;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AiSystemDto {
    private UUID id;
    private String name;
    private String description;
    private String vendor;
    private String version;
    private String purpose;
    private String deploymentContext;
    private String organizationRole;
    private String status;
    private String riskLevel;
    private Integer complianceScore;
    private String complianceStatus;
    private Instant classifiedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static AiSystemDto from(AiSystem system) {
        return AiSystemDto.builder()
                .id(system.getId())
                .name(system.getName())
                .description(system.getDescription())
                .vendor(system.getVendor())
                .version(system.getVersion())
                .purpose(system.getPurpose())
                .deploymentContext(system.getDeploymentContext() != null ? system.getDeploymentContext().name() : null)
                .organizationRole(system.getOrganizationRole() != null ? system.getOrganizationRole().name() : null)
                .status(system.getStatus().name())
                .riskLevel(system.getRiskLevel() != null ? system.getRiskLevel().name() : null)
                .complianceScore(system.getComplianceScore())
                .complianceStatus(system.getComplianceStatus().name())
                .classifiedAt(system.getClassifiedAt())
                .createdAt(system.getCreatedAt())
                .updatedAt(system.getUpdatedAt())
                .build();
    }
}
