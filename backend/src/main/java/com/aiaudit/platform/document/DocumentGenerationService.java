package com.aiaudit.platform.document;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemService;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.common.exception.SubscriptionLimitException;
import com.aiaudit.platform.document.dto.*;
import com.aiaudit.platform.organization.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerationService {

    private final GeneratedDocumentRepository documentRepository;
    private final AiSystemService aiSystemService;
    private final ClaudeApiService claudeApiService;
    private final PromptTemplateService promptTemplateService;

    private static final int STARTER_MONTHLY_LIMIT = 3;

    @Transactional(readOnly = true)
    public List<DocumentDto> getDocuments(UUID aiSystemId, UUID organizationId) {
        aiSystemService.findByIdAndOrg(aiSystemId, organizationId);
        return documentRepository.findByAiSystemIdOrderByCreatedAtDesc(aiSystemId).stream()
                .map(DocumentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID documentId) {
        GeneratedDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        return DocumentDto.from(doc);
    }

    @Transactional
    public DocumentDto startGeneration(UUID aiSystemId, UUID organizationId,
                                        GenerateDocumentRequest request, AppUser user) {
        AiSystem system = aiSystemService.findByIdAndOrg(aiSystemId, organizationId);

        // Check subscription limits
        checkDocumentLimit(organizationId, system.getOrganization().getSubscriptionPlan());

        String title = promptTemplateService.getDocumentTitle(request.getDocumentType(), system.getName());

        GeneratedDocument doc = GeneratedDocument.builder()
                .aiSystem(system)
                .documentType(request.getDocumentType())
                .title(title)
                .status(DocumentStatus.GENERATING)
                .generatedBy(user)
                .build();
        doc = documentRepository.save(doc);

        // Trigger async generation
        generateContentAsync(doc.getId(), system, request.getDocumentType(), request.getAdditionalContext());

        return DocumentDto.from(doc);
    }

    @Async
    @Transactional
    public void generateContentAsync(UUID documentId, AiSystem system,
                                      DocumentType type, String additionalContext) {
        try {
            String prompt = promptTemplateService.buildPrompt(type, system, additionalContext);
            String content = claudeApiService.generateContent(prompt);

            GeneratedDocument doc = documentRepository.findById(documentId).orElseThrow();
            doc.setContent(content);
            doc.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(doc);
            log.info("Document generated successfully: {}", documentId);
        } catch (Exception e) {
            log.error("Document generation failed for {}", documentId, e);
            documentRepository.findById(documentId).ifPresent(doc -> {
                doc.setStatus(DocumentStatus.FAILED);
                doc.setContent("Generation failed: " + e.getMessage());
                documentRepository.save(doc);
            });
        }
    }

    @Transactional
    public DocumentDto updateDocument(UUID documentId, UpdateDocumentRequest request) {
        GeneratedDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getContent() != null) doc.setContent(request.getContent());

        return DocumentDto.from(documentRepository.save(doc));
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document", "id", documentId);
        }
        documentRepository.deleteById(documentId);
    }

    @Transactional
    public DocumentDto regenerateDocument(UUID documentId, AppUser user) {
        GeneratedDocument original = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        AiSystem system = original.getAiSystem();
        checkDocumentLimit(system.getOrganization().getId(), system.getOrganization().getSubscriptionPlan());

        GeneratedDocument newDoc = GeneratedDocument.builder()
                .aiSystem(system)
                .documentType(original.getDocumentType())
                .title(original.getTitle())
                .status(DocumentStatus.GENERATING)
                .version(original.getVersion() + 1)
                .parentId(original.getId())
                .generatedBy(user)
                .build();
        newDoc = documentRepository.save(newDoc);

        generateContentAsync(newDoc.getId(), system, original.getDocumentType(), null);
        return DocumentDto.from(newDoc);
    }

    private void checkDocumentLimit(UUID organizationId, SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.STARTER) {
            YearMonth now = YearMonth.now();
            Instant monthStart = now.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            long count = documentRepository.countByOrganizationIdAndCreatedAtAfter(organizationId, monthStart);
            if (count >= STARTER_MONTHLY_LIMIT) {
                throw new SubscriptionLimitException("Starter plan is limited to " + STARTER_MONTHLY_LIMIT + " documents per month. Upgrade to Professional for unlimited documents.");
            }
        }
    }
}
