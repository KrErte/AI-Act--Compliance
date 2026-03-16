package com.aiaudit.platform.classification;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemService;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.classification.dto.*;
import com.aiaudit.platform.compliance.ComplianceObligationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final ClassificationQuestionRepository questionRepository;
    private final ClassificationResponseRepository responseRepository;
    private final ClassificationEngine classificationEngine;
    private final AiSystemService aiSystemService;
    private final ComplianceObligationService complianceObligationService;

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions() {
        return questionRepository.findByActiveTrueOrderBySortOrder().stream()
                .map(QuestionDto::from)
                .toList();
    }

    @Transactional
    public ClassificationResult classifyAiSystem(UUID aiSystemId, UUID organizationId,
                                                  Map<String, String> answers, AppUser user) {
        AiSystem system = aiSystemService.findByIdAndOrg(aiSystemId, organizationId);

        // Clear previous responses
        responseRepository.deleteAllByAiSystemId(aiSystemId);

        // Save new responses
        answers.forEach((questionKey, answer) -> {
            questionRepository.findByQuestionKey(questionKey).ifPresent(question -> {
                ClassificationResponse response = ClassificationResponse.builder()
                        .aiSystem(system)
                        .question(question)
                        .answer(answer)
                        .build();
                responseRepository.save(response);
            });
        });

        // Run classification
        ClassificationResult result = classificationEngine.classify(answers);

        // Update AI system
        system.setRiskLevel(result.getRiskLevel());
        system.setClassifiedAt(Instant.now());
        system.setClassifiedBy(user);

        // Generate compliance obligations from classification result
        complianceObligationService.generateObligations(system, result);

        return result;
    }

    /**
     * Public classification — no persistence, no auth needed
     */
    public ClassificationResult classifyPublic(Map<String, String> answers) {
        return classificationEngine.classify(answers);
    }
}
