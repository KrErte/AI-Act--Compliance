package com.aiaudit.platform.regulation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class AssessmentResultDto {
    private UUID id;
    private UUID regulationId;
    private String regulationName;
    private double overallScore;
    private List<DomainScore> domainScores;
    private Instant completedAt;

    @Data
    @Builder
    public static class DomainScore {
        private String domainName;
        private String domainCode;
        private double score;
        private double weight;
        private int answeredQuestions;
        private int totalQuestions;
    }
}
