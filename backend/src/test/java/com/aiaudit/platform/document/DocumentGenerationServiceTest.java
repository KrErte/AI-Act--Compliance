package com.aiaudit.platform.document;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemService;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.common.exception.SubscriptionLimitException;
import com.aiaudit.platform.document.dto.DocumentDto;
import com.aiaudit.platform.document.dto.GenerateDocumentRequest;
import com.aiaudit.platform.document.dto.UpdateDocumentRequest;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentGenerationServiceTest {

    @Mock
    private GeneratedDocumentRepository documentRepository;

    @Mock
    private AiSystemService aiSystemService;

    @Mock
    private ClaudeApiService claudeApiService;

    @Mock
    private PromptTemplateService promptTemplateService;

    @InjectMocks
    private DocumentGenerationService service;

    private Organization organization;
    private AiSystem aiSystem;
    private AppUser user;
    private GeneratedDocument document;

    @BeforeEach
    void setUp() {
        organization = TestDataBuilder.anOrganization()
                .subscriptionPlan(SubscriptionPlan.PROFESSIONAL)
                .build();
        aiSystem = TestDataBuilder.anAiSystem()
                .organization(organization)
                .build();
        user = TestDataBuilder.anAppUser()
                .organization(organization)
                .build();
        document = TestDataBuilder.aDocument()
                .aiSystem(aiSystem)
                .generatedBy(user)
                .build();
    }

    // ── getDocuments ──

    @Nested
    @DisplayName("getDocuments")
    class GetDocumentsTests {

        @Test
        @DisplayName("returns list of DocumentDto for a valid AI system")
        void returnsDocumentList() {
            UUID aiSystemId = aiSystem.getId();
            UUID orgId = organization.getId();

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(aiSystem);
            when(documentRepository.findByAiSystemIdOrderByCreatedAtDesc(aiSystemId))
                    .thenReturn(List.of(document));

            List<DocumentDto> result = service.getDocuments(aiSystemId, orgId);

            assertEquals(1, result.size());
            assertEquals(document.getId(), result.get(0).getId());
            verify(aiSystemService).findByIdAndOrg(aiSystemId, orgId);
        }

        @Test
        @DisplayName("returns empty list when no documents exist")
        void returnsEmptyList() {
            UUID aiSystemId = aiSystem.getId();
            UUID orgId = organization.getId();

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(aiSystem);
            when(documentRepository.findByAiSystemIdOrderByCreatedAtDesc(aiSystemId))
                    .thenReturn(List.of());

            List<DocumentDto> result = service.getDocuments(aiSystemId, orgId);

            assertTrue(result.isEmpty());
        }
    }

    // ── getDocument ──

    @Nested
    @DisplayName("getDocument")
    class GetDocumentTests {

        @Test
        @DisplayName("returns DocumentDto for existing document")
        void returnsDocument() {
            UUID docId = document.getId();
            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

            DocumentDto result = service.getDocument(docId);

            assertNotNull(result);
            assertEquals(docId, result.getId());
            assertEquals(document.getTitle(), result.getTitle());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent document")
        void throwsWhenNotFound() {
            UUID docId = UUID.randomUUID();
            when(documentRepository.findById(docId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.getDocument(docId));
        }
    }

    // ── startGeneration ──

    @Nested
    @DisplayName("startGeneration")
    class StartGenerationTests {

        @Test
        @DisplayName("creates document with GENERATING status for PROFESSIONAL plan")
        void createsDocumentProfessional() {
            UUID aiSystemId = aiSystem.getId();
            UUID orgId = organization.getId();

            GenerateDocumentRequest request = new GenerateDocumentRequest();
            request.setDocumentType(DocumentType.FRIA);
            request.setAdditionalContext("some context");

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(aiSystem);
            when(promptTemplateService.getDocumentTitle(DocumentType.FRIA, aiSystem.getName()))
                    .thenReturn("Fundamental Rights Impact Assessment — Test AI System");
            when(documentRepository.save(any(GeneratedDocument.class))).thenAnswer(inv -> {
                GeneratedDocument doc = inv.getArgument(0);
                doc.setId(UUID.randomUUID());
                doc.setCreatedAt(Instant.now());
                doc.setUpdatedAt(Instant.now());
                return doc;
            });

            DocumentDto result = service.startGeneration(aiSystemId, orgId, request, user);

            assertNotNull(result);
            assertEquals("GENERATING", result.getStatus());
            verify(documentRepository).save(any(GeneratedDocument.class));
        }

        @Test
        @DisplayName("throws SubscriptionLimitException when STARTER plan exceeds monthly limit")
        void throwsWhenStarterLimitExceeded() {
            Organization starterOrg = TestDataBuilder.anOrganization()
                    .subscriptionPlan(SubscriptionPlan.STARTER)
                    .build();
            AiSystem starterSystem = TestDataBuilder.anAiSystem()
                    .organization(starterOrg)
                    .build();
            AppUser starterUser = TestDataBuilder.anAppUser()
                    .organization(starterOrg)
                    .build();

            UUID aiSystemId = starterSystem.getId();
            UUID orgId = starterOrg.getId();

            GenerateDocumentRequest request = new GenerateDocumentRequest();
            request.setDocumentType(DocumentType.FRIA);

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(starterSystem);
            when(documentRepository.countByOrganizationIdAndCreatedAtAfter(eq(orgId), any(Instant.class)))
                    .thenReturn(3L);

            assertThrows(SubscriptionLimitException.class,
                    () -> service.startGeneration(aiSystemId, orgId, request, starterUser));
        }

        @Test
        @DisplayName("allows STARTER plan when under monthly limit")
        void allowsStarterUnderLimit() {
            Organization starterOrg = TestDataBuilder.anOrganization()
                    .subscriptionPlan(SubscriptionPlan.STARTER)
                    .build();
            AiSystem starterSystem = TestDataBuilder.anAiSystem()
                    .organization(starterOrg)
                    .build();
            AppUser starterUser = TestDataBuilder.anAppUser()
                    .organization(starterOrg)
                    .build();

            UUID aiSystemId = starterSystem.getId();
            UUID orgId = starterOrg.getId();

            GenerateDocumentRequest request = new GenerateDocumentRequest();
            request.setDocumentType(DocumentType.TECHNICAL_DOC);

            when(aiSystemService.findByIdAndOrg(aiSystemId, orgId)).thenReturn(starterSystem);
            when(documentRepository.countByOrganizationIdAndCreatedAtAfter(eq(orgId), any(Instant.class)))
                    .thenReturn(2L);
            when(promptTemplateService.getDocumentTitle(DocumentType.TECHNICAL_DOC, starterSystem.getName()))
                    .thenReturn("Technical Documentation — Test AI System");
            when(documentRepository.save(any(GeneratedDocument.class))).thenAnswer(inv -> {
                GeneratedDocument doc = inv.getArgument(0);
                doc.setId(UUID.randomUUID());
                doc.setCreatedAt(Instant.now());
                doc.setUpdatedAt(Instant.now());
                return doc;
            });

            DocumentDto result = service.startGeneration(aiSystemId, orgId, request, starterUser);

            assertNotNull(result);
            assertEquals("GENERATING", result.getStatus());
        }
    }

    // ── generateContentAsync ──

    @Nested
    @DisplayName("generateContentAsync")
    class GenerateContentAsyncTests {

        @Test
        @DisplayName("builds prompt, calls Claude, saves content with COMPLETED status")
        void generatesContentSuccessfully() {
            UUID docId = document.getId();
            String prompt = "Generated prompt";
            String content = "# Generated Content\n\nSome compliance content.";

            when(promptTemplateService.buildPrompt(DocumentType.FRIA, aiSystem, null))
                    .thenReturn(prompt);
            when(claudeApiService.generateContent(prompt)).thenReturn(content);
            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(GeneratedDocument.class))).thenReturn(document);

            service.generateContentAsync(docId, aiSystem, DocumentType.FRIA, null);

            verify(documentRepository).save(argThat(doc ->
                    doc.getContent().equals(content) &&
                    doc.getStatus() == DocumentStatus.COMPLETED
            ));
        }

        @Test
        @DisplayName("includes additional context in prompt")
        void includesAdditionalContext() {
            UUID docId = document.getId();
            String additionalContext = "Focus on healthcare";
            String prompt = "Generated prompt with context";

            when(promptTemplateService.buildPrompt(DocumentType.FRIA, aiSystem, additionalContext))
                    .thenReturn(prompt);
            when(claudeApiService.generateContent(prompt)).thenReturn("content");
            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(GeneratedDocument.class))).thenReturn(document);

            service.generateContentAsync(docId, aiSystem, DocumentType.FRIA, additionalContext);

            verify(promptTemplateService).buildPrompt(DocumentType.FRIA, aiSystem, additionalContext);
        }

        @Test
        @DisplayName("sets FAILED status on Claude API failure")
        void setsFailedOnError() {
            UUID docId = document.getId();

            when(promptTemplateService.buildPrompt(DocumentType.FRIA, aiSystem, null))
                    .thenReturn("prompt");
            when(claudeApiService.generateContent("prompt"))
                    .thenThrow(new RuntimeException("API error"));
            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(GeneratedDocument.class))).thenReturn(document);

            service.generateContentAsync(docId, aiSystem, DocumentType.FRIA, null);

            verify(documentRepository).save(argThat(doc ->
                    doc.getStatus() == DocumentStatus.FAILED
            ));
        }
    }

    // ── updateDocument ──

    @Nested
    @DisplayName("updateDocument")
    class UpdateDocumentTests {

        @Test
        @DisplayName("updates title and content")
        void updatesDocument() {
            UUID docId = document.getId();
            UpdateDocumentRequest request = new UpdateDocumentRequest();
            request.setTitle("Updated Title");
            request.setContent("Updated Content");

            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(GeneratedDocument.class))).thenReturn(document);

            DocumentDto result = service.updateDocument(docId, request);

            assertNotNull(result);
            verify(documentRepository).save(any(GeneratedDocument.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent document")
        void throwsWhenNotFound() {
            UUID docId = UUID.randomUUID();
            UpdateDocumentRequest request = new UpdateDocumentRequest();
            request.setTitle("Title");

            when(documentRepository.findById(docId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateDocument(docId, request));
        }
    }

    // ── deleteDocument ──

    @Nested
    @DisplayName("deleteDocument")
    class DeleteDocumentTests {

        @Test
        @DisplayName("deletes existing document")
        void deletesDocument() {
            UUID docId = document.getId();
            when(documentRepository.existsById(docId)).thenReturn(true);

            service.deleteDocument(docId);

            verify(documentRepository).deleteById(docId);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent document")
        void throwsWhenNotFound() {
            UUID docId = UUID.randomUUID();
            when(documentRepository.existsById(docId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> service.deleteDocument(docId));
        }
    }

    // ── regenerateDocument ──

    @Nested
    @DisplayName("regenerateDocument")
    class RegenerateDocumentTests {

        @Test
        @DisplayName("creates new version with incremented version number")
        void createsNewVersion() {
            UUID docId = document.getId();
            document.setVersion(1);

            when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(GeneratedDocument.class))).thenAnswer(inv -> {
                GeneratedDocument doc = inv.getArgument(0);
                doc.setId(UUID.randomUUID());
                doc.setCreatedAt(Instant.now());
                doc.setUpdatedAt(Instant.now());
                return doc;
            });

            DocumentDto result = service.regenerateDocument(docId, user);

            assertNotNull(result);
            assertEquals("GENERATING", result.getStatus());
            verify(documentRepository).save(argThat(doc ->
                    doc.getVersion() == 2 &&
                    doc.getParentId().equals(docId) &&
                    doc.getStatus() == DocumentStatus.GENERATING
            ));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent document")
        void throwsWhenNotFound() {
            UUID docId = UUID.randomUUID();
            when(documentRepository.findById(docId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.regenerateDocument(docId, user));
        }
    }
}
