package com.aiaudit.platform.dashboard;

import com.aiaudit.platform.aisystem.AiSystemRepository;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.compliance.ComplianceObligationRepository;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.dashboard.dto.DashboardSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AiSystemRepository aiSystemRepository;
    private final ComplianceObligationRepository obligationRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getSummary(UUID organizationId) {
        long totalSystems = aiSystemRepository.countByOrganizationIdAndDeletedFalse(organizationId);

        // Risk distribution
        Map<String, Long> riskDistribution = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            riskDistribution.put(level.name(), aiSystemRepository.countByOrganizationIdAndRiskLevel(organizationId, level));
        }

        // Obligation counts across all AI systems in the org
        var allObligations = obligationRepository.findUpcomingByOrganizationId(organizationId);
        long totalObligations = allObligations.size();
        long completedObligations = allObligations.stream()
                .filter(o -> o.getStatus() == ObligationStatus.COMPLETED).count();
        long inProgressObligations = allObligations.stream()
                .filter(o -> o.getStatus() == ObligationStatus.IN_PROGRESS).count();
        long notStartedObligations = allObligations.stream()
                .filter(o -> o.getStatus() == ObligationStatus.NOT_STARTED).count();

        int complianceScore = totalObligations > 0 ? (int) ((completedObligations * 100) / totalObligations) : 0;

        // Upcoming deadlines (first 5)
        var deadlines = allObligations.stream()
                .filter(o -> o.getDueDate() != null && o.getStatus() != ObligationStatus.COMPLETED)
                .limit(5)
                .map(o -> new DashboardSummary.Deadline(
                        o.getArticleTitle(),
                        o.getDueDate().toString(),
                        o.getAiSystem().getName()
                ))
                .toList();

        return DashboardSummary.builder()
                .totalAiSystems(totalSystems)
                .riskDistribution(riskDistribution)
                .overallComplianceScore(complianceScore)
                .obligationCounts(DashboardSummary.ObligationCounts.builder()
                        .total(totalObligations)
                        .completed(completedObligations)
                        .inProgress(inProgressObligations)
                        .notStarted(notStartedObligations)
                        .build())
                .upcomingDeadlines(deadlines)
                .build();
    }
}
