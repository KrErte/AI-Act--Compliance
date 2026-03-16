package com.aiaudit.platform.classification;

import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.classification.dto.ClassificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassificationEngineTest {

    private ClassificationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ClassificationEngine();
    }

    private Map<String, String> allNo() {
        Map<String, String> answers = new HashMap<>();
        answers.put("PROHIBITED_SOCIAL_SCORING", "NO");
        answers.put("PROHIBITED_SUBLIMINAL", "NO");
        answers.put("PROHIBITED_VULNERABILITY", "NO");
        answers.put("PROHIBITED_FACIAL_SCRAPING", "NO");
        answers.put("PROHIBITED_EMOTION_WORKPLACE", "NO");
        answers.put("PROHIBITED_BIOMETRIC_CATEGORIZATION", "NO");
        answers.put("PROHIBITED_PREDICTIVE_POLICING", "NO");
        answers.put("HIGH_BIOMETRICS", "NO");
        answers.put("HIGH_CRITICAL_INFRASTRUCTURE", "NO");
        answers.put("HIGH_EDUCATION", "NO");
        answers.put("HIGH_EMPLOYMENT", "NO");
        answers.put("HIGH_ESSENTIAL_SERVICES", "NO");
        answers.put("HIGH_LAW_ENFORCEMENT", "NO");
        answers.put("HIGH_MIGRATION", "NO");
        answers.put("HIGH_JUSTICE", "NO");
        answers.put("HIGH_SAFETY_COMPONENT", "NO");
        answers.put("HIGH_SAFETY_THIRD_PARTY", "NO");
        answers.put("LIMITED_CHATBOT", "NO");
        answers.put("LIMITED_SYNTHETIC_CONTENT", "NO");
        answers.put("LIMITED_EMOTION_RECOGNITION", "NO");
        answers.put("EXEMPTION_NARROW_PROCEDURAL", "NO");
        answers.put("EXEMPTION_IMPROVE_HUMAN_ACTIVITY", "NO");
        answers.put("EXEMPTION_PREPARATORY_TASK", "NO");
        answers.put("EXEMPTION_DETECT_PATTERNS", "NO");
        return answers;
    }

    @Nested
    @DisplayName("Prohibited AI (Article 5)")
    class ProhibitedTests {

        @Test
        @DisplayName("Social scoring → UNACCEPTABLE")
        void socialScoring() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_SOCIAL_SCORING", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("5(1)(c)")));
        }

        @Test
        @DisplayName("Subliminal manipulation → UNACCEPTABLE")
        void subliminalManipulation() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_SUBLIMINAL", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Vulnerability exploitation → UNACCEPTABLE")
        void vulnerabilityExploitation() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_VULNERABILITY", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Facial scraping → UNACCEPTABLE")
        void facialScraping() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_FACIAL_SCRAPING", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Emotion recognition in workplace → UNACCEPTABLE")
        void emotionWorkplace() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_EMOTION_WORKPLACE", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Biometric categorization → UNACCEPTABLE")
        void biometricCategorization() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_BIOMETRIC_CATEGORIZATION", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Predictive policing → UNACCEPTABLE")
        void predictivePolicing() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_PREDICTIVE_POLICING", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("Prohibited takes priority over high-risk")
        void prohibitedPriorityOverHighRisk() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_SOCIAL_SCORING", "YES");
            answers.put("HIGH_EMPLOYMENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }
    }

    @Nested
    @DisplayName("High-Risk Annex III")
    class HighRiskAnnexIIITests {

        @Test
        @DisplayName("Biometrics → HIGH")
        void biometrics() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_BIOMETRICS", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("Biometrics")));
        }

        @Test
        @DisplayName("Critical infrastructure → HIGH")
        void criticalInfrastructure() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_CRITICAL_INFRASTRUCTURE", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Employment → HIGH")
        void employment() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EMPLOYMENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            assertFalse(result.getRecommendedActions().isEmpty());
        }

        @Test
        @DisplayName("Education → HIGH")
        void education() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EDUCATION", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Essential services → HIGH")
        void essentialServices() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_ESSENTIAL_SERVICES", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Law enforcement → HIGH")
        void lawEnforcement() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_LAW_ENFORCEMENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Migration → HIGH")
        void migration() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_MIGRATION", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Justice → HIGH")
        void justice() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_JUSTICE", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Annex III with all exemptions → falls through to next check (not HIGH)")
        void annexIIIWithExemptions() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EDUCATION", "YES");
            answers.put("EXEMPTION_NARROW_PROCEDURAL", "YES");
            answers.put("EXEMPTION_IMPROVE_HUMAN_ACTIVITY", "YES");
            answers.put("EXEMPTION_PREPARATORY_TASK", "YES");
            answers.put("EXEMPTION_DETECT_PATTERNS", "YES");
            ClassificationResult result = engine.classify(answers);
            // Should NOT be HIGH because exempted
            assertNotEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Annex III with partial exemptions → still HIGH")
        void annexIIIWithPartialExemptions() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EMPLOYMENT", "YES");
            answers.put("EXEMPTION_NARROW_PROCEDURAL", "YES");
            // Missing other exemptions
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("HIGH risk includes obligation articles")
        void highRiskIncludesArticles() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EMPLOYMENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("Article 9")));
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("Article 17")));
        }
    }

    @Nested
    @DisplayName("High-Risk Safety Component (Article 6(1))")
    class HighRiskSafetyTests {

        @Test
        @DisplayName("Safety component → HIGH")
        void safetyComponent() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_SAFETY_COMPONENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("6(1)")));
        }

        @Test
        @DisplayName("Third-party conformity → HIGH")
        void thirdPartyConformity() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_SAFETY_THIRD_PARTY", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }
    }

    @Nested
    @DisplayName("Limited Risk (Article 50)")
    class LimitedRiskTests {

        @Test
        @DisplayName("Chatbot → LIMITED")
        void chatbot() {
            Map<String, String> answers = allNo();
            answers.put("LIMITED_CHATBOT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.LIMITED, result.getRiskLevel());
            assertTrue(result.getApplicableArticles().stream().anyMatch(a -> a.contains("50(1)")));
        }

        @Test
        @DisplayName("Synthetic content → LIMITED")
        void syntheticContent() {
            Map<String, String> answers = allNo();
            answers.put("LIMITED_SYNTHETIC_CONTENT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.LIMITED, result.getRiskLevel());
        }

        @Test
        @DisplayName("Emotion recognition (non-prohibited) → LIMITED")
        void emotionRecognition() {
            Map<String, String> answers = allNo();
            answers.put("LIMITED_EMOTION_RECOGNITION", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.LIMITED, result.getRiskLevel());
        }
    }

    @Nested
    @DisplayName("Minimal Risk (default)")
    class MinimalRiskTests {

        @Test
        @DisplayName("All NO → MINIMAL")
        void allNo() {
            Map<String, String> answers = ClassificationEngineTest.this.allNo();
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.MINIMAL, result.getRiskLevel());
            assertNotNull(result.getRationale());
        }

        @Test
        @DisplayName("Empty answers → MINIMAL")
        void emptyAnswers() {
            ClassificationResult result = engine.classify(Map.of());
            assertEquals(RiskLevel.MINIMAL, result.getRiskLevel());
        }
    }

    @Nested
    @DisplayName("Priority & Edge Cases")
    class PriorityTests {

        @Test
        @DisplayName("Prohibited > HIGH > LIMITED priority")
        void priorityOrder() {
            Map<String, String> answers = allNo();
            answers.put("PROHIBITED_SOCIAL_SCORING", "YES");
            answers.put("HIGH_EMPLOYMENT", "YES");
            answers.put("LIMITED_CHATBOT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.UNACCEPTABLE, result.getRiskLevel());
        }

        @Test
        @DisplayName("HIGH > LIMITED priority")
        void highOverLimited() {
            Map<String, String> answers = allNo();
            answers.put("HIGH_EMPLOYMENT", "YES");
            answers.put("LIMITED_CHATBOT", "YES");
            ClassificationResult result = engine.classify(answers);
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }

        @Test
        @DisplayName("Result always has rationale, articles, and actions")
        void resultCompleteness() {
            for (RiskLevel level : RiskLevel.values()) {
                Map<String, String> answers = allNo();
                switch (level) {
                    case UNACCEPTABLE -> answers.put("PROHIBITED_SOCIAL_SCORING", "YES");
                    case HIGH -> answers.put("HIGH_EMPLOYMENT", "YES");
                    case LIMITED -> answers.put("LIMITED_CHATBOT", "YES");
                    case MINIMAL -> {} // all NO
                }
                ClassificationResult result = engine.classify(answers);
                assertNotNull(result.getRationale(), "Rationale missing for " + level);
                assertNotNull(result.getApplicableArticles(), "Articles missing for " + level);
                assertFalse(result.getApplicableArticles().isEmpty(), "Articles empty for " + level);
                assertNotNull(result.getRecommendedActions(), "Actions missing for " + level);
                assertFalse(result.getRecommendedActions().isEmpty(), "Actions empty for " + level);
                assertNotNull(result.getDeadline(), "Deadline missing for " + level);
            }
        }
    }
}
