package com.aiaudit.platform.document;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.aisystem.AiSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class PromptTemplateServiceTest {

    private PromptTemplateService service;
    private AiSystem aiSystem;

    @BeforeEach
    void setUp() {
        service = new PromptTemplateService();
        aiSystem = TestDataBuilder.anAiSystem()
                .name("MySystem")
                .description("A recruitment screening tool")
                .vendor("TechCorp")
                .version("2.0")
                .purpose("Automated CV screening")
                .build();
    }

    // ── buildPrompt ──

    @Nested
    @DisplayName("buildPrompt")
    class BuildPromptTests {

        @Test
        @DisplayName("FRIA prompt contains AI system details")
        void friaPromptContainsSystemDetails() {
            String prompt = service.buildPrompt(DocumentType.FRIA, aiSystem, null);

            assertNotNull(prompt);
            assertTrue(prompt.contains("MySystem"));
            assertTrue(prompt.contains("A recruitment screening tool"));
            assertTrue(prompt.contains("TechCorp"));
            assertTrue(prompt.contains("Fundamental Rights Impact Assessment"));
        }

        @Test
        @DisplayName("TECHNICAL_DOC prompt includes additional context when provided")
        void technicalDocIncludesAdditionalContext() {
            String prompt = service.buildPrompt(DocumentType.TECHNICAL_DOC, aiSystem, "extra context");

            assertNotNull(prompt);
            assertTrue(prompt.contains("extra context"));
            assertTrue(prompt.contains("Technical Documentation"));
            assertTrue(prompt.contains("MySystem"));
        }

        @Test
        @DisplayName("prompt without additional context does not contain 'Additional context' label")
        void promptWithoutAdditionalContext() {
            String prompt = service.buildPrompt(DocumentType.FRIA, aiSystem, null);

            assertFalse(prompt.contains("Additional context:"));
        }

        @ParameterizedTest
        @EnumSource(DocumentType.class)
        @DisplayName("all document types return non-null prompt containing system name")
        void allTypesReturnValidPrompt(DocumentType type) {
            String prompt = service.buildPrompt(type, aiSystem, null);

            assertNotNull(prompt, "Prompt should not be null for type " + type);
            assertFalse(prompt.isBlank(), "Prompt should not be blank for type " + type);
            assertTrue(prompt.contains("MySystem"), "Prompt should contain system name for type " + type);
        }
    }

    // ── getDocumentTitle ──

    @Nested
    @DisplayName("getDocumentTitle")
    class GetDocumentTitleTests {

        @Test
        @DisplayName("FRIA title follows correct format")
        void friaTitle() {
            String title = service.getDocumentTitle(DocumentType.FRIA, "MySystem");

            assertEquals("Fundamental Rights Impact Assessment \u2014 MySystem", title);
        }

        @Test
        @DisplayName("TECHNICAL_DOC title follows correct format")
        void technicalDocTitle() {
            String title = service.getDocumentTitle(DocumentType.TECHNICAL_DOC, "MySystem");

            assertEquals("Technical Documentation \u2014 MySystem", title);
        }

        @Test
        @DisplayName("RISK_MANAGEMENT title follows correct format")
        void riskManagementTitle() {
            String title = service.getDocumentTitle(DocumentType.RISK_MANAGEMENT, "MySystem");

            assertEquals("Risk Management Plan \u2014 MySystem", title);
        }

        @Test
        @DisplayName("HUMAN_OVERSIGHT title follows correct format")
        void humanOversightTitle() {
            String title = service.getDocumentTitle(DocumentType.HUMAN_OVERSIGHT, "MySystem");

            assertEquals("Human Oversight Protocol \u2014 MySystem", title);
        }

        @Test
        @DisplayName("DATA_GOVERNANCE title follows correct format")
        void dataGovernanceTitle() {
            String title = service.getDocumentTitle(DocumentType.DATA_GOVERNANCE, "MySystem");

            assertEquals("Data Governance Plan \u2014 MySystem", title);
        }

        @Test
        @DisplayName("CONFORMITY_DECLARATION title follows correct format")
        void conformityDeclarationTitle() {
            String title = service.getDocumentTitle(DocumentType.CONFORMITY_DECLARATION, "MySystem");

            assertEquals("EU Declaration of Conformity \u2014 MySystem", title);
        }

        @Test
        @DisplayName("POST_MARKET_MONITORING title follows correct format")
        void postMarketMonitoringTitle() {
            String title = service.getDocumentTitle(DocumentType.POST_MARKET_MONITORING, "MySystem");

            assertEquals("Post-Market Monitoring Plan \u2014 MySystem", title);
        }

        @Test
        @DisplayName("TRANSPARENCY_NOTICE title follows correct format")
        void transparencyNoticeTitle() {
            String title = service.getDocumentTitle(DocumentType.TRANSPARENCY_NOTICE, "MySystem");

            assertEquals("Transparency Notice \u2014 MySystem", title);
        }
    }
}
