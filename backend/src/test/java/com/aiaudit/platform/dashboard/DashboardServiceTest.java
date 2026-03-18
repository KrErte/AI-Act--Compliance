package com.aiaudit.platform.dashboard;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemRepository;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.compliance.ComplianceObligation;
import com.aiaudit.platform.compliance.ComplianceObligationRepository;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.dashboard.dto.DashboardSummary;
import com.aiaudit.platform.organization.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AiSystemRepository aiSystemRepository;

    @Mock
    private ComplianceObligationRepository obligationRepository;

    @InjectMocks
    private DashboardService service;

    private Organization organization;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        organization = TestDataBuilder.anOrganization().build();
        orgId = organization.getId();
    }

    @Test
    @DisplayName("returns summary with correct total AI systems and risk distribution")
    void returnsSummaryWithTotalsAndRiskDistribution() {
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(5L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.MINIMAL)).thenReturn(1L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.LIMITED)).thenReturn(1L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.HIGH)).thenReturn(2L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.UNACCEPTABLE)).thenReturn(1L);
        when(obligationRepository.findUpcomingByOrganizationId(orgId)).thenReturn(List.of());

        DashboardSummary summary = service.getSummary(orgId);

        assertEquals(5L, summary.getTotalAiSystems());
        assertEquals(1L, summary.getRiskDistribution().get("MINIMAL"));
        assertEquals(1L, summary.getRiskDistribution().get("LIMITED"));
        assertEquals(2L, summary.getRiskDistribution().get("HIGH"));
        assertEquals(1L, summary.getRiskDistribution().get("UNACCEPTABLE"));
    }

    @Test
    @DisplayName("returns zeros when no AI systems exist")
    void returnsZerosForEmptyOrg() {
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(0L);
        for (RiskLevel level : RiskLevel.values()) {
            when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, level)).thenReturn(0L);
        }
        when(obligationRepository.findUpcomingByOrganizationId(orgId)).thenReturn(List.of());

        DashboardSummary summary = service.getSummary(orgId);

        assertEquals(0L, summary.getTotalAiSystems());
        assertEquals(0, summary.getOverallComplianceScore());
        assertEquals(0L, summary.getObligationCounts().getTotal());
        assertEquals(0L, summary.getObligationCounts().getCompleted());
        assertEquals(0L, summary.getObligationCounts().getInProgress());
        assertEquals(0L, summary.getObligationCounts().getNotStarted());
        assertTrue(summary.getUpcomingDeadlines().isEmpty());
    }

    @Test
    @DisplayName("calculates compliance score as (completed / total) * 100")
    void calculatesComplianceScore() {
        AiSystem system = TestDataBuilder.anAiSystem()
                .organization(organization)
                .build();

        // Create 10 obligations: 3 completed, 4 in-progress, 3 not-started
        List<ComplianceObligation> obligations = List.of(
                buildObligation(system, ObligationStatus.COMPLETED, "Art. 9", null),
                buildObligation(system, ObligationStatus.COMPLETED, "Art. 10", null),
                buildObligation(system, ObligationStatus.COMPLETED, "Art. 11", null),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 12", LocalDate.now().plusDays(30)),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 13", LocalDate.now().plusDays(60)),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 14", LocalDate.now().plusDays(90)),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 15", LocalDate.now().plusDays(120)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 17", LocalDate.now().plusDays(150)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 27", LocalDate.now().plusDays(180)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 43", LocalDate.now().plusDays(210))
        );

        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(1L);
        for (RiskLevel level : RiskLevel.values()) {
            when(aiSystemRepository.countByOrganizationIdAndRiskLevel(eq(orgId), eq(level)))
                    .thenReturn(level == RiskLevel.HIGH ? 1L : 0L);
        }
        when(obligationRepository.findUpcomingByOrganizationId(orgId)).thenReturn(obligations);

        DashboardSummary summary = service.getSummary(orgId);

        assertEquals(30, summary.getOverallComplianceScore()); // 3/10 = 30%
        assertEquals(10L, summary.getObligationCounts().getTotal());
        assertEquals(3L, summary.getObligationCounts().getCompleted());
        assertEquals(4L, summary.getObligationCounts().getInProgress());
        assertEquals(3L, summary.getObligationCounts().getNotStarted());
    }

    @Test
    @DisplayName("limits upcoming deadlines to 5 items, ordered by due date")
    void limitsUpcomingDeadlines() {
        AiSystem system = TestDataBuilder.anAiSystem()
                .organization(organization)
                .name("HR Screening AI")
                .build();

        // 7 obligations with due dates (not completed) -- repo returns ordered by dueDate ASC
        List<ComplianceObligation> obligations = List.of(
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 9", LocalDate.of(2026, 4, 1)),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 10", LocalDate.of(2026, 5, 1)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 11", LocalDate.of(2026, 5, 15)),
                buildObligation(system, ObligationStatus.IN_PROGRESS, "Art. 12", LocalDate.of(2026, 6, 1)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 13", LocalDate.of(2026, 6, 15)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 14", LocalDate.of(2026, 7, 1)),
                buildObligation(system, ObligationStatus.NOT_STARTED, "Art. 15", LocalDate.of(2026, 7, 15))
        );

        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(1L);
        for (RiskLevel level : RiskLevel.values()) {
            when(aiSystemRepository.countByOrganizationIdAndRiskLevel(eq(orgId), eq(level)))
                    .thenReturn(level == RiskLevel.HIGH ? 1L : 0L);
        }
        when(obligationRepository.findUpcomingByOrganizationId(orgId)).thenReturn(obligations);

        DashboardSummary summary = service.getSummary(orgId);

        assertEquals(5, summary.getUpcomingDeadlines().size());
        assertEquals("HR Screening AI", summary.getUpcomingDeadlines().get(0).getAiSystemName());
    }

    @Test
    @DisplayName("risk distribution counts per RiskLevel correctly")
    void riskDistributionCountsPerLevel() {
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(10L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.MINIMAL)).thenReturn(3L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.LIMITED)).thenReturn(2L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.HIGH)).thenReturn(4L);
        when(aiSystemRepository.countByOrganizationIdAndRiskLevel(orgId, RiskLevel.UNACCEPTABLE)).thenReturn(1L);
        when(obligationRepository.findUpcomingByOrganizationId(orgId)).thenReturn(List.of());

        DashboardSummary summary = service.getSummary(orgId);

        assertEquals(4, summary.getRiskDistribution().size());
        assertEquals(3L, summary.getRiskDistribution().get("MINIMAL"));
        assertEquals(2L, summary.getRiskDistribution().get("LIMITED"));
        assertEquals(4L, summary.getRiskDistribution().get("HIGH"));
        assertEquals(1L, summary.getRiskDistribution().get("UNACCEPTABLE"));
    }

    // ── Helper ──

    private ComplianceObligation buildObligation(AiSystem system, ObligationStatus status,
                                                  String articleRef, LocalDate dueDate) {
        return TestDataBuilder.anObligation()
                .aiSystem(system)
                .status(status)
                .articleRef(articleRef)
                .articleTitle(articleRef + " — Obligation")
                .dueDate(dueDate)
                .build();
    }
}
