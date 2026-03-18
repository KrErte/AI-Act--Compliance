package com.aiaudit.platform.classification;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemService;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.classification.dto.ClassificationResult;
import com.aiaudit.platform.classification.dto.QuestionDto;
import com.aiaudit.platform.compliance.ComplianceObligationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationServiceTest {

    @Mock
    private ClassificationQuestionRepository questionRepository;

    @Mock
    private ClassificationResponseRepository responseRepository;

    @Mock
    private ClassificationEngine classificationEngine;

    @Mock
    private AiSystemService aiSystemService;

    @Mock
    private ComplianceObligationService complianceObligationService;

    @InjectMocks
    private ClassificationService service;

    private AiSystem aiSystem;
    private AppUser user;

    @BeforeEach
    void setUp() {
        aiSystem = TestDataBuilder.anAiSystem().build();
        user = TestDataBuilder.anAppUser()
                .organization(aiSystem.getOrganization())
                .build();
    }

    // ── getQuestions ──

    @Nested
    @DisplayName("getQuestions")
    class GetQuestionsTests {

        @Test
        @DisplayName("returns list of QuestionDto from active questions")
        void returnsQuestionList() {
            ClassificationQuestion q1 = ClassificationQuestion.builder()
                    .id(UUID.randomUUID())
                    .questionKey("PROHIBITED_SOCIAL_SCORING")
                    .questionText("Does the system perform social scoring?")
                    .questionType(QuestionType.YES_NO)
                    .category("Prohibited")
                    .sortOrder(1)
                    .active(true)
                    .build();

            ClassificationQuestion q2 = ClassificationQuestion.builder()
                    .id(UUID.randomUUID())
                    .questionKey("HIGH_BIOMETRICS")
                    .questionText("Does the system use biometric identification?")
                    .questionType(QuestionType.YES_NO)
                    .category("High Risk")
                    .sortOrder(2)
                    .active(true)
                    .build();

            when(questionRepository.findByActiveTrueOrderBySortOrder())
                    .thenReturn(List.of(q1, q2));

            List<QuestionDto> result = service.getQuestions();

            assertEquals(2, result.size());
            assertEquals("PROHIBITED_SOCIAL_SCORING", result.get(0).getQuestionKey());
            assertEquals("HIGH_BIOMETRICS", result.get(1).getQuestionKey());
        }

        @Test
        @DisplayName("returns empty list when no active questions exist")
        void returnsEmptyList() {
            when(questionRepository.findByActiveTrueOrderBySortOrder())
                    .thenReturn(List.of());

            List<QuestionDto> result = service.getQuestions();

            assertTrue(result.isEmpty());
        }
    }

    // ── classifyAiSystem ──

    @Nested
    @DisplayName("classifyAiSystem")
    class ClassifyAiSystemTests {

        @Test
        @DisplayName("clears previous responses, saves new ones, runs classification, and updates system")
        void fullClassificationFlow() {
            UUID aiSystemId = aiSystem.getId();
            UUID orgId = aiSystem.getOrganization().getId();
            Map<String, String> answers = Map.of("HIGH_EMPLOYMENT", "YES");

            ClassificationQuestion question = ClassificationQuestion.builder()
                    .id(UUID.randomUUID())
                    .questionKey("HIGH_EMPLOYMENT")
                    .questionText("Employment use?")
                    .questionType(QuestionType.YES_NO)
                    .category("High Risk")
                    .sortOrder(1)
                    .active(true)
                    .build();

            ClassificationResult expectedResult = ClassificationResult.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .rationale("High risk under Annex III")
                    .applicableArticles(List.of("Annex III, Area 4 — Employment"))
                    .recommendedActions(List.of("Establish risk management system"))
                    .deadline("2 August 2026")
                    .build();

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(aiSystem);
            when(questionRepository.findByQuestionKey("HIGH_EMPLOYMENT"))
                    .thenReturn(Optional.of(question));
            when(classificationEngine.classify(answers)).thenReturn(expectedResult);

            ClassificationResult result = service.classifyAiSystem(aiSystemId, orgId, answers, user);

            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            verify(responseRepository).deleteAllByAiSystemId(aiSystemId);
            verify(responseRepository).save(any(ClassificationResponse.class));
            verify(complianceObligationService).generateObligations(eq(aiSystem), eq(expectedResult));
            assertEquals(RiskLevel.HIGH, aiSystem.getRiskLevel());
            assertNotNull(aiSystem.getClassifiedAt());
            assertEquals(user, aiSystem.getClassifiedBy());
        }

        @Test
        @DisplayName("handles empty answers without error")
        void handlesEmptyAnswers() {
            UUID aiSystemId = aiSystem.getId();
            UUID orgId = aiSystem.getOrganization().getId();
            Map<String, String> emptyAnswers = Map.of();

            ClassificationResult minimalResult = ClassificationResult.builder()
                    .riskLevel(RiskLevel.MINIMAL)
                    .rationale("Minimal risk")
                    .applicableArticles(List.of("Article 95 — Voluntary codes of conduct"))
                    .recommendedActions(List.of("Consider adopting voluntary codes"))
                    .deadline("N/A")
                    .build();

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(aiSystem);
            when(classificationEngine.classify(emptyAnswers)).thenReturn(minimalResult);

            ClassificationResult result = service.classifyAiSystem(aiSystemId, orgId, emptyAnswers, user);

            assertEquals(RiskLevel.MINIMAL, result.getRiskLevel());
            verify(responseRepository).deleteAllByAiSystemId(aiSystemId);
            verify(responseRepository, never()).save(any());
        }
    }

    // ── classifyPublic ──

    @Nested
    @DisplayName("classifyPublic")
    class ClassifyPublicTests {

        @Test
        @DisplayName("delegates to classification engine without persistence")
        void delegatesToEngine() {
            Map<String, String> answers = Map.of("LIMITED_CHATBOT", "YES");

            ClassificationResult expectedResult = ClassificationResult.builder()
                    .riskLevel(RiskLevel.LIMITED)
                    .rationale("Limited risk")
                    .applicableArticles(List.of("Article 50(1)"))
                    .recommendedActions(List.of("Implement disclosure"))
                    .deadline("2 August 2026")
                    .build();

            when(classificationEngine.classify(answers)).thenReturn(expectedResult);

            ClassificationResult result = service.classifyPublic(answers);

            assertEquals(RiskLevel.LIMITED, result.getRiskLevel());
            verify(classificationEngine).classify(answers);
            verifyNoInteractions(aiSystemService, responseRepository, questionRepository, complianceObligationService);
        }
    }
}
