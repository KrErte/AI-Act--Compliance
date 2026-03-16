package com.aiaudit.platform.classification.dto;

import com.aiaudit.platform.classification.ClassificationQuestion;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class QuestionDto {
    private UUID id;
    private String questionKey;
    private String questionText;
    private String questionType;
    private String options;
    private String category;
    private String helpText;
    private int sortOrder;
    private String dependsOn;
    private String dependsOnAnswer;

    public static QuestionDto from(ClassificationQuestion q) {
        return QuestionDto.builder()
                .id(q.getId())
                .questionKey(q.getQuestionKey())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType().name())
                .options(q.getOptions())
                .category(q.getCategory())
                .helpText(q.getHelpText())
                .sortOrder(q.getSortOrder())
                .dependsOn(q.getDependsOn())
                .dependsOnAnswer(q.getDependsOnAnswer())
                .build();
    }
}
