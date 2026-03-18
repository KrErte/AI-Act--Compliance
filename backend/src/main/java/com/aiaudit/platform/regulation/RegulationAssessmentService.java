package com.aiaudit.platform.regulation;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.regulation.dto.AssessmentResultDto;
import com.aiaudit.platform.regulation.dto.SubmitAssessmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegulationAssessmentService {

    private final RegulationRepository regulationRepository;
    private final RegulationAssessmentRepository assessmentRepository;
    private final RegulationQuestionRepository questionRepository;

    @Transactional
    public AssessmentResultDto submitAssessment(SubmitAssessmentRequest request, AppUser user) {
        Regulation regulation = regulationRepository.findById(request.getRegulationId())
                .orElseThrow(() -> new ResourceNotFoundException("Regulation not found"));

        RegulationAssessment assessment = RegulationAssessment.builder()
                .organization(user.getOrganization())
                .regulation(regulation)
                .build();

        List<RegulationAnswer> answers = new ArrayList<>();
        for (SubmitAssessmentRequest.AnswerInput input : request.getAnswers()) {
            RegulationQuestion question = questionRepository.findById(input.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + input.getQuestionId()));
            answers.add(RegulationAnswer.builder()
                    .assessment(assessment)
                    .question(question)
                    .answer(input.getAnswer())
                    .notes(input.getNotes())
                    .build());
        }
        assessment.setAnswers(answers);

        // Calculate scores
        AssessmentResultDto result = calculateScores(assessment, regulation);
        assessment.setOverallScore(result.getOverallScore());
        assessment.setCompletedAt(Instant.now());

        assessment = assessmentRepository.save(assessment);
        return result.toBuilder().id(assessment.getId()).build();
    }

    @Transactional(readOnly = true)
    public AssessmentResultDto getLatestAssessment(UUID regulationId, UUID organizationId) {
        RegulationAssessment assessment = assessmentRepository
                .findTopByOrganizationIdAndRegulationIdOrderByCreatedAtDesc(organizationId, regulationId)
                .orElseThrow(() -> new ResourceNotFoundException("No assessment found for this regulation"));

        Regulation regulation = assessment.getRegulation();
        return calculateScores(assessment, regulation).toBuilder()
                .id(assessment.getId())
                .completedAt(assessment.getCompletedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AssessmentResultDto> getAssessmentHistory(UUID organizationId) {
        return assessmentRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(a -> AssessmentResultDto.builder()
                        .id(a.getId())
                        .regulationId(a.getRegulation().getId())
                        .regulationName(a.getRegulation().getName())
                        .overallScore(a.getOverallScore() != null ? a.getOverallScore() : 0)
                        .completedAt(a.getCompletedAt())
                        .build())
                .toList();
    }

    private AssessmentResultDto calculateScores(RegulationAssessment assessment, Regulation regulation) {
        // Group answers by domain
        Map<UUID, List<RegulationAnswer>> answersByDomain = assessment.getAnswers().stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getDomain().getId()));

        List<AssessmentResultDto.DomainScore> domainScores = new ArrayList<>();
        double totalWeightedScore = 0;
        double totalWeight = 0;

        for (RegulationDomain domain : regulation.getDomains()) {
            List<RegulationAnswer> domainAnswers = answersByDomain.getOrDefault(domain.getId(), List.of());
            int totalQuestions = domain.getQuestions().size();
            int answered = domainAnswers.size();

            double domainScore = 0;
            if (answered > 0) {
                double sum = domainAnswers.stream().mapToInt(RegulationAnswer::getAnswer).sum();
                domainScore = (sum / (answered * 4.0)) * 100; // Scale to 0-100%
            }

            domainScores.add(AssessmentResultDto.DomainScore.builder()
                    .domainName(domain.getName())
                    .domainCode(domain.getCode())
                    .score(Math.round(domainScore * 10) / 10.0)
                    .weight(domain.getWeight())
                    .answeredQuestions(answered)
                    .totalQuestions(totalQuestions)
                    .build());

            totalWeightedScore += domainScore * domain.getWeight();
            totalWeight += domain.getWeight();
        }

        double overallScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0;

        return AssessmentResultDto.builder()
                .regulationId(regulation.getId())
                .regulationName(regulation.getName())
                .overallScore(Math.round(overallScore * 10) / 10.0)
                .domainScores(domainScores)
                .build();
    }
}
