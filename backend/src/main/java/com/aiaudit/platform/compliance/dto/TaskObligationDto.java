package com.aiaudit.platform.compliance.dto;

import com.aiaudit.platform.compliance.ComplianceObligation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TaskObligationDto {
    private UUID id;
    private String articleRef;
    private String articleTitle;
    private String description;
    private String status;
    private LocalDate dueDate;
    private UUID aiSystemId;
    private String aiSystemName;

    public static TaskObligationDto from(ComplianceObligation obligation) {
        return TaskObligationDto.builder()
                .id(obligation.getId())
                .articleRef(obligation.getArticleRef())
                .articleTitle(obligation.getArticleTitle())
                .description(obligation.getDescription())
                .status(obligation.getStatus().name())
                .dueDate(obligation.getDueDate())
                .aiSystemId(obligation.getAiSystem().getId())
                .aiSystemName(obligation.getAiSystem().getName())
                .build();
    }
}
