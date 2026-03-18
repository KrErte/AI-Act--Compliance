package com.aiaudit.platform.regulation.dto;

import com.aiaudit.platform.regulation.Regulation;
import com.aiaudit.platform.regulation.RegulationDomain;
import com.aiaudit.platform.regulation.RegulationQuestion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RegulationDetailDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private LocalDate effectiveDate;
    private List<DomainDto> domains;

    @Data
    @Builder
    public static class DomainDto {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private double weight;
        private List<QuestionDto> questions;
    }

    @Data
    @Builder
    public static class QuestionDto {
        private UUID id;
        private String questionEn;
        private String questionEt;
        private String articleRef;
        private String explanationEn;
        private String recommendationEn;
    }

    public static RegulationDetailDto from(Regulation r) {
        return RegulationDetailDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .effectiveDate(r.getEffectiveDate())
                .domains(r.getDomains().stream().map(d -> DomainDto.builder()
                        .id(d.getId())
                        .code(d.getCode())
                        .name(d.getName())
                        .description(d.getDescription())
                        .weight(d.getWeight())
                        .questions(d.getQuestions().stream().map(q -> QuestionDto.builder()
                                .id(q.getId())
                                .questionEn(q.getQuestionEn())
                                .questionEt(q.getQuestionEt())
                                .articleRef(q.getArticleRef())
                                .explanationEn(q.getExplanationEn())
                                .recommendationEn(q.getRecommendationEn())
                                .build()).toList())
                        .build()).toList())
                .build();
    }
}
