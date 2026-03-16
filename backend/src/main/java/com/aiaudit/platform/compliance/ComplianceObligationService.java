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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ComplianceObligationService {

    private final ComplianceObligationRepository obligationRepository;
    private final AiSystemService aiSystemService;
    private final ComplianceScoreService complianceScoreService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ObligationDto> getObligations(UUID aiSystemId, UUID organizationId) {
        // Verify access
        aiSystemService.findByIdAndOrg(aiSystemId, organizationId);
        return obligationRepository.findByAiSystemIdOrderBySortOrder(aiSystemId).stream()
                .map(ObligationDto::from)
                .toList();
    }

    @Transactional
    public ObligationDto updateObligation(UUID obligationId, UUID aiSystemId, UUID organizationId,
                                           UpdateObligationRequest request, AppUser currentUser) {
        AiSystem system = aiSystemService.findByIdAndOrg(aiSystemId, organizationId);

        ComplianceObligation obligation = obligationRepository.findByIdAndAiSystemId(obligationId, aiSystemId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligation", "id", obligationId));

        String oldStatus = obligation.getStatus().name();

        if (request.getStatus() != null) obligation.setStatus(request.getStatus());
        if (request.getDueDate() != null) obligation.setDueDate(request.getDueDate());
        if (request.getNotes() != null) obligation.setNotes(request.getNotes());
        if (request.getAssignedTo() != null) {
            AppUser assignee = userRepository.findById(request.getAssignedTo()).orElse(null);
            obligation.setAssignedTo(assignee);
        }

        ObligationDto result = ObligationDto.from(obligationRepository.save(obligation));

        auditService.log("OBLIGATION", obligationId, "UPDATED",
                oldStatus, obligation.getStatus().name(),
                Map.of("articleRef", obligation.getArticleRef(), "aiSystemId", aiSystemId),
                currentUser, system.getOrganization());

        // Recalculate compliance score
        complianceScoreService.recalculate(aiSystemId);

        return result;
    }

    @Transactional
    public void generateObligations(AiSystem system, ClassificationResult result) {
        // Clear existing obligations
        obligationRepository.deleteAllByAiSystemId(system.getId());

        if (result.getRiskLevel() == RiskLevel.MINIMAL) return;

        AtomicInteger order = new AtomicInteger(0);
        result.getApplicableArticles().forEach(article -> {
            ComplianceObligation obligation = ComplianceObligation.builder()
                    .aiSystem(system)
                    .articleRef(extractArticleRef(article))
                    .articleTitle(article)
                    .description(generateDescription(article, result.getRiskLevel()))
                    .sortOrder(order.getAndIncrement())
                    .build();
            obligationRepository.save(obligation);
        });

        // Recalculate score
        complianceScoreService.recalculate(system.getId());
    }

    private String extractArticleRef(String article) {
        // Extract "Article X" or "Annex III" from strings like "Article 9 — Risk Management System"
        int dashIndex = article.indexOf('—');
        if (dashIndex > 0) return article.substring(0, dashIndex).trim();
        return article.length() > 50 ? article.substring(0, 50) : article;
    }

    private String generateDescription(String article, RiskLevel riskLevel) {
        return String.format("Compliance obligation under %s for %s risk AI system. " +
                "Review the AI Act regulation for detailed requirements.", article, riskLevel);
    }
}
