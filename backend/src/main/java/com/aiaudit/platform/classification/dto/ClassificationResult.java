package com.aiaudit.platform.classification.dto;

import com.aiaudit.platform.aisystem.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassificationResult {
    private RiskLevel riskLevel;
    private String rationale;
    private List<String> applicableArticles;
    private List<String> recommendedActions;
    private String deadline;
}
