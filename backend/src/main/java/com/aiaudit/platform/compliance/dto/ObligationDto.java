package com.aiaudit.platform.compliance.dto;

import com.aiaudit.platform.compliance.ComplianceObligation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ObligationDto {
    private UUID id;
    private UUID aiSystemId;
    private String articleRef;
    private String articleTitle;
    private String description;
    private String status;
    private UUID assignedTo;
    private LocalDate dueDate;
    private String notes;
    private int sortOrder;

    public static ObligationDto from(ComplianceObligation obligation) {
        return ObligationDto.builder()
                .id(obligation.getId())
                .aiSystemId(obligation.getAiSystem().getId())
                .articleRef(obligation.getArticleRef())
                .articleTitle(obligation.getArticleTitle())
                .description(obligation.getDescription())
                .status(obligation.getStatus().name())
                .assignedTo(obligation.getAssignedTo() != null ? obligation.getAssignedTo().getId() : null)
                .dueDate(obligation.getDueDate())
                .notes(obligation.getNotes())
                .sortOrder(obligation.getSortOrder())
                .build();
    }
}
