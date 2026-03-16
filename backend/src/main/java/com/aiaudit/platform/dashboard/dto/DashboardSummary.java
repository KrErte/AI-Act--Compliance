package com.aiaudit.platform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long totalAiSystems;
    private Map<String, Long> riskDistribution;
    private int overallComplianceScore;
    private ObligationCounts obligationCounts;
    private List<Deadline> upcomingDeadlines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObligationCounts {
        private long total;
        private long completed;
        private long inProgress;
        private long notStarted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Deadline {
        private String title;
        private String date;
        private String aiSystemName;
    }
}
