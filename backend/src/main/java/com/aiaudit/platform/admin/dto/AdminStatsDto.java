package com.aiaudit.platform.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private long totalAiSystems;
    private int overallComplianceScore;
    private long totalObligations;
    private long completedObligations;
    private Map<String, Long> riskDistribution;
    private Map<String, Long> usersByRole;
}
