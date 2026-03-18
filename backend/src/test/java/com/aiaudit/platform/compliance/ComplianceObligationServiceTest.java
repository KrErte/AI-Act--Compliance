package com.aiaudit.platform.compliance;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemService;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.audit.AuditService;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.UserRepository;
import com.aiaudit.platform.classification.dto.ClassificationResult;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.compliance.dto.ObligationDto;
import com.aiaudit.platform.compliance.dto.UpdateObligationRequest;
import com.aiaudit.platform.organization.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceObligationServiceTest {

    @Mock
    private ComplianceObligationRepository obligationRepository;

    @Mock
    private AiSystemService aiSystemService;

    @Mock
    private ComplianceScoreService complianceScoreService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ComplianceObligationService complianceObligationService;

    @Captor
    private ArgumentCaptor<ComplianceObligation> obligationCaptor;

    private Organization organization;
    private AiSystem aiSystem;
    private AppUser user;
    private UUID orgId;
    private UUID systemId;

    @BeforeEach
    void setUp() {
        organization = anOrganization().build();
        orgId = organization.getId();
        aiSystem = anAiSystem().organization(organization).build();
        systemId = aiSystem.getId();
        user = anAppUser().organization(organization).build();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // getObligations
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getObligations")
    class GetObligations {

        @Test
        @DisplayName("should verify access and return obligation DTOs")
        void returnsObligationDtos() {
            ComplianceObligation obligation = anObligation().aiSystem(aiSystem).build();

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByAiSystemIdOrderBySortOrder(systemId))
                    .thenReturn(List.of(obligation));

            List<ObligationDto> result = complianceObligationService.getObligations(systemId, orgId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getArticleRef()).isEqualTo(obligation.getArticleRef());
            assertThat(result.get(0).getAiSystemId()).isEqualTo(systemId);
            verify(aiSystemService).findByIdAndOrg(systemId, orgId);
        }

        @Test
        @DisplayName("should return empty list when no obligations exist")
        void returnsEmptyList() {
            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByAiSystemIdOrderBySortOrder(systemId))
                    .thenReturn(List.of());

            List<ObligationDto> result = complianceObligationService.getObligations(systemId, orgId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when AI system not found for organization")
        void throwsWhenSystemNotFound() {
            when(aiSystemService.findByIdAndOrg(systemId, orgId))
                    .thenThrow(new ResourceNotFoundException("AI System", "id", systemId));

            assertThatThrownBy(() -> complianceObligationService.getObligations(systemId, orgId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(obligationRepository, never()).findByAiSystemIdOrderBySortOrder(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // updateObligation
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateObligation")
    class UpdateObligation {

        @Test
        @DisplayName("should update status and recalculate score")
        void updatesStatusAndRecalculates() {
            ComplianceObligation obligation = anObligation()
                    .aiSystem(aiSystem)
                    .status(ObligationStatus.NOT_STARTED)
                    .build();
            UUID oblId = obligation.getId();

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByIdAndAiSystemId(oblId, systemId))
                    .thenReturn(Optional.of(obligation));
            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.COMPLETED);

            ObligationDto result = complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user);

            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            verify(complianceScoreService).recalculate(systemId);
            verify(auditService).log(eq("OBLIGATION"), eq(oblId), eq("UPDATED"),
                    eq("NOT_STARTED"), eq("COMPLETED"), any(), eq(user), eq(organization));
        }

        @Test
        @DisplayName("should update due date")
        void updatesDueDate() {
            ComplianceObligation obligation = anObligation().aiSystem(aiSystem).build();
            UUID oblId = obligation.getId();
            LocalDate newDueDate = LocalDate.of(2026, 8, 1);

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByIdAndAiSystemId(oblId, systemId))
                    .thenReturn(Optional.of(obligation));
            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setDueDate(newDueDate);

            ObligationDto result = complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user);

            assertThat(result.getDueDate()).isEqualTo(newDueDate);
        }

        @Test
        @DisplayName("should update notes")
        void updatesNotes() {
            ComplianceObligation obligation = anObligation().aiSystem(aiSystem).build();
            UUID oblId = obligation.getId();

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByIdAndAiSystemId(oblId, systemId))
                    .thenReturn(Optional.of(obligation));
            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setNotes("Reviewed by legal team");

            ObligationDto result = complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user);

            assertThat(result.getNotes()).isEqualTo("Reviewed by legal team");
        }

        @Test
        @DisplayName("should assign user when assignedTo is provided")
        void assignsUser() {
            ComplianceObligation obligation = anObligation().aiSystem(aiSystem).build();
            UUID oblId = obligation.getId();
            AppUser assignee = anAppUser().email("assignee@test.com").build();
            UUID assigneeId = assignee.getId();

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByIdAndAiSystemId(oblId, systemId))
                    .thenReturn(Optional.of(obligation));
            when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setAssignedTo(assigneeId);

            ObligationDto result = complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user);

            assertThat(result.getAssignedTo()).isEqualTo(assigneeId);
        }

        @Test
        @DisplayName("should throw when obligation not found")
        void throwsWhenObligationNotFound() {
            UUID oblId = UUID.randomUUID();

            when(aiSystemService.findByIdAndOrg(systemId, orgId)).thenReturn(aiSystem);
            when(obligationRepository.findByIdAndAiSystemId(oblId, systemId))
                    .thenReturn(Optional.empty());

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.COMPLETED);

            assertThatThrownBy(() -> complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Obligation");

            verify(obligationRepository, never()).save(any());
            verify(complianceScoreService, never()).recalculate(any());
        }

        @Test
        @DisplayName("should throw when AI system not found for update")
        void throwsWhenSystemNotFoundForUpdate() {
            UUID oblId = UUID.randomUUID();

            when(aiSystemService.findByIdAndOrg(systemId, orgId))
                    .thenThrow(new ResourceNotFoundException("AI System", "id", systemId));

            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.IN_PROGRESS);

            assertThatThrownBy(() -> complianceObligationService.updateObligation(
                    oblId, systemId, orgId, request, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(obligationRepository, never()).findByIdAndAiSystemId(any(), any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // generateObligations
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateObligations")
    class GenerateObligations {

        @Test
        @DisplayName("should clear existing and generate obligations for HIGH risk")
        void generatesForHighRisk() {
            ClassificationResult classification = ClassificationResult.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .applicableArticles(List.of(
                            "Article 9 \u2014 Risk Management System",
                            "Article 10 \u2014 Data Governance",
                            "Article 13 \u2014 Transparency"))
                    .build();

            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            complianceObligationService.generateObligations(aiSystem, classification);

            verify(obligationRepository).deleteAllByAiSystemId(systemId);
            verify(obligationRepository, times(3)).save(obligationCaptor.capture());

            List<ComplianceObligation> saved = obligationCaptor.getAllValues();
            assertThat(saved).hasSize(3);
            assertThat(saved.get(0).getArticleRef()).isEqualTo("Article 9");
            assertThat(saved.get(0).getSortOrder()).isZero();
            assertThat(saved.get(1).getSortOrder()).isEqualTo(1);
            assertThat(saved.get(2).getSortOrder()).isEqualTo(2);
            assertThat(saved.get(0).getAiSystem()).isSameAs(aiSystem);

            verify(complianceScoreService).recalculate(systemId);
        }

        @Test
        @DisplayName("should clear existing but not generate for MINIMAL risk")
        void skipsGenerationForMinimalRisk() {
            ClassificationResult classification = ClassificationResult.builder()
                    .riskLevel(RiskLevel.MINIMAL)
                    .applicableArticles(List.of())
                    .build();

            complianceObligationService.generateObligations(aiSystem, classification);

            verify(obligationRepository).deleteAllByAiSystemId(systemId);
            verify(obligationRepository, never()).save(any());
            verify(complianceScoreService, never()).recalculate(any());
        }

        @Test
        @DisplayName("should generate obligations for LIMITED risk")
        void generatesForLimitedRisk() {
            ClassificationResult classification = ClassificationResult.builder()
                    .riskLevel(RiskLevel.LIMITED)
                    .applicableArticles(List.of("Article 52 \u2014 Transparency Obligations"))
                    .build();

            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            complianceObligationService.generateObligations(aiSystem, classification);

            verify(obligationRepository).deleteAllByAiSystemId(systemId);
            verify(obligationRepository, times(1)).save(any(ComplianceObligation.class));
            verify(complianceScoreService).recalculate(systemId);
        }

        @Test
        @DisplayName("should generate obligations for UNACCEPTABLE risk")
        void generatesForUnacceptableRisk() {
            ClassificationResult classification = ClassificationResult.builder()
                    .riskLevel(RiskLevel.UNACCEPTABLE)
                    .applicableArticles(List.of("Article 5 \u2014 Prohibited AI Practices"))
                    .build();

            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            complianceObligationService.generateObligations(aiSystem, classification);

            verify(obligationRepository).deleteAllByAiSystemId(systemId);
            verify(obligationRepository, times(1)).save(obligationCaptor.capture());

            ComplianceObligation saved = obligationCaptor.getValue();
            assertThat(saved.getArticleRef()).isEqualTo("Article 5");
            assertThat(saved.getDescription()).contains("UNACCEPTABLE");
            verify(complianceScoreService).recalculate(systemId);
        }

        @Test
        @DisplayName("should set correct description containing article and risk level")
        void setsCorrectDescription() {
            ClassificationResult classification = ClassificationResult.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .applicableArticles(List.of("Article 9 \u2014 Risk Management System"))
                    .build();

            when(obligationRepository.save(any(ComplianceObligation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            complianceObligationService.generateObligations(aiSystem, classification);

            verify(obligationRepository).save(obligationCaptor.capture());
            ComplianceObligation saved = obligationCaptor.getValue();
            assertThat(saved.getDescription())
                    .contains("Article 9")
                    .contains("Risk Management System")
                    .contains("HIGH");
        }
    }
}
